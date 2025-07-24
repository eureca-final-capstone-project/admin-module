package eureca.capstone.project.admin.report.service.impl;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.admin.common.exception.custom.*;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.transaction_feed.repository.TransactionFeedRepository;
import eureca.capstone.project.admin.user.repository.UserRepository;
import eureca.capstone.project.admin.report.entity.ReportHistory;
import eureca.capstone.project.admin.report.entity.ReportType;
import eureca.capstone.project.admin.report.entity.RestrictionTarget;
import eureca.capstone.project.admin.report.entity.RestrictionType;
import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.report.dto.request.AIReviewRequestDto;
import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.report.dto.response.*;
import eureca.capstone.project.admin.report.repository.*;
import eureca.capstone.project.admin.report.service.ReportService;
import eureca.capstone.project.admin.report.service.external.AIReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static eureca.capstone.project.admin.common.constant.ReportConst.*;
import static eureca.capstone.project.admin.common.constant.RestrictionConst.*;
import static eureca.capstone.project.admin.common.entity.StatusConst.*;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportHistoryRepository reportHistoryRepository;
    private final RestrictionTargetRepository restrictionTargetRepository;
    private final RestrictionTypeRepository restrictionTypeRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final UserRepository userRepository;
    private final TransactionFeedRepository transactionFeedRepository;
    private final AIReviewService aiReviewService;
    private final StatusManager statusManager;

    @Override
    public ReportCountResponseDto getReportCounts() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long todayCount = reportHistoryRepository.countByCreatedAtAfter(startOfToday);
        long totalCount = reportHistoryRepository.count();

        log.info("[getReportCounts] todayCount: {}, totalCount: {}", todayCount, totalCount);
        return ReportCountResponseDto.builder()
                .todayReportCount(todayCount)
                .totalReportCount(totalCount)
                .build();
    }

    @Override
    public Page<ReportHistoryDto> getReportHistoryListByStatusCode(String statusCode,String keyword, Pageable pageable) {
        Page<ReportHistory> reportHistories = reportHistoryRepository.findByCriteria(statusCode, keyword, pageable);
        log.info("[getReportHistoryListByStatusCode] 신고 내역 조회 (keyword: {}, statusCode: {}): 총 {} 건", keyword, statusCode, reportHistories.getTotalElements());
        return reportHistories.map(ReportHistoryDto::from);
    }

    @Override
    @Transactional
    public void processReportByAdmin(Long reportHistoryId, ProcessReportDto request) {
        ReportHistory reportHistory = reportHistoryRepository.findById(reportHistoryId)
                .orElseThrow(ReportNotFoundException::new);

        Long pendingStatusId = statusManager.getStatus(REPORT,"PENDING").getStatusId();
        Long aiRejectStatusId = statusManager.getStatus(REPORT,"AI_REJECTED").getStatusId();

        // 이미 처리된 신고
        List<Long> processableStatus = List.of(pendingStatusId, aiRejectStatusId);
        if (!processableStatus.contains(reportHistory.getStatus().getStatusId())) {
            throw new AlreadyProcessedReportException();
        }

        // 신고 거절
        if (!request.getApproved()) {
            Status adminRejectedStatus = statusManager.getStatus(REPORT, "ADMIN_REJECTED");
            reportHistory.updateStatus(adminRejectedStatus);

            log.info("[processReportByAdmin] 관리자 신고 거절. status={}", reportHistory.getStatus().getCode());
            return;
        }

        // 신고 승인
        Status adminAcceptedStatus = statusManager.getStatus(REPORT, "ADMIN_ACCEPTED");
        reportHistory.updateStatus(adminAcceptedStatus);

        log.info("[processReportByAdmin] 관리자 신고 승인. status={}", reportHistory.getStatus().getCode());
        checkAndApplyRestriction(reportHistory.getSeller(), reportHistory.getReportType());
    }

    @Override
    @Transactional
    public void createReportAndProcessWithAI(Long userId, Long transactionFeedId, Long reportTypeId, String reason) {
        ReportType reportType = reportTypeRepository.findById(reportTypeId)
                .orElseThrow(ReportTypeNotFoundException::new);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        TransactionFeed transactionFeed = transactionFeedRepository.findById(transactionFeedId)
                .orElseThrow(TransactionFeedNotFoundException::new);
        User seller = transactionFeed.getUser();

        if(reportHistoryRepository.existsByUserAndSeller(user, seller)){
            throw new DuplicateReportException();
        }

        log.info("[신고 접수] 신고자: {}, 피드 제목: [{}], 피드 내용: [{}], 신고 유형: {}, 신고 이유: {}",
                user.getEmail(),
                transactionFeed.getTitle(),
                transactionFeed.getContent(),
                reportType.getType(),
                reason);

        AIReviewRequestDto requestDto = new AIReviewRequestDto(
                transactionFeed.getTitle(),
                transactionFeed.getContent(),
                reason,
                reportType.getType()
        );

        AIReviewResponseDto aiResponse = aiReviewService.requestReview(requestDto);
        Status initialStatus = getReportHistoryStatus(aiResponse);
        log.info("[createReportAndProcessWithAI] AI 판단 결과 initialStatus={}", initialStatus.getDescription());

        ReportHistory newReport = ReportHistory.builder()
                .user(user) // 신고자
                .transactionFeed(transactionFeed)
                .seller(seller) // 피신고자
                .reportType(reportType)
                .reason(reason)
                .isModerated(true)
                .status(initialStatus)
                .build();
        reportHistoryRepository.save(newReport);

        log.info("[createReportAndProcessWithAI] AI 판단 이후 신고내역 추가. 피신고자: {}", seller.getEmail());

        Status aiAcceptedStatus = statusManager.getStatus(REPORT, "AI_ACCEPTED");
        if (initialStatus.equals(aiAcceptedStatus)) {
            log.info("[createReportAndProcessWithAI] AI 승인 완료");
            checkAndApplyRestriction(seller, reportType);
        }
    }


    private Status getReportHistoryStatus(AIReviewResponseDto aiResponse) {
        if (aiResponse.getConfidence() < 0.8) {
            log.info("[getReportHistoryStatus] 신고내용 AI 판단 모호 (신뢰도 : {})", aiResponse.getConfidence());
            return statusManager.getStatus(REPORT,"PENDING");
        } else {
            return switch (aiResponse.getResult()) {
                case "ACCEPT" -> statusManager.getStatus(REPORT, "AI_ACCEPTED");
                case "REJECT" -> statusManager.getStatus(REPORT, "AI_REJECTED");
                default -> statusManager.getStatus(REPORT,"PENDING");
            };
        }
    }

    private void checkAndApplyRestriction(User seller, ReportType reportType) {
        Status aiAcceptStatus = statusManager.getStatus(REPORT, "AI_ACCEPTED");
        Status adminAcceptStatus = statusManager.getStatus(REPORT, "ADMIN_ACCEPTED");
        List<Status> acceptedStatuses = List.of(aiAcceptStatus, adminAcceptStatus);

        long violationCount = reportHistoryRepository
                .countReportToRestrict(seller, reportType, acceptedStatuses);

        RestrictionType restrictionType;
        log.info("[checkAndApplyRestriction] 피신고 수={}", violationCount);

        switch (reportType.getReportTypeId().intValue()) {
            case BAD_WORD -> { // 욕설 및 비속어
                if (violationCount >= BAD_WORD_CNT) {
                    applyRestriction(seller, reportType, RES_WRITE_7DAYS, acceptedStatuses);
                }
            }
            case TOPIC_MISMATCH -> {  // 주제 불일치
                if (violationCount >= TOPIC_MISMATCH_CNT) {
                    applyRestriction(seller, reportType, RES_WRITE_1DAYS, acceptedStatuses);
                }
            }
            case OBSCENE -> applyRestriction(seller, reportType, RES_FOREVER, acceptedStatuses); // 음란 내용 포함
            case EXTERNAL_CHANNEL -> { // 외부 채널 유도
                if (violationCount >= EXTERNAL_CHANNEL_CNT) {
                    applyRestriction(seller, reportType, RES_TRANSACTION, acceptedStatuses);
                }
            }
            case SNIPING -> { // 비방/저격 포함
                if (violationCount >= SNIPING_CNT) {
                    applyRestriction(seller, reportType, RES_WRITE_7DAYS, acceptedStatuses);
                }
            }
        }
    }

    private void applyRestriction(User user, ReportType reportType, Long restrictionTypeId, List<Status> acceptedStatuses) {
        RestrictionType restrictionType = restrictionTypeRepository.findById(restrictionTypeId)
                .orElseThrow(RestrictionTypeNotFoundException::new);

        log.info("[applyRestriction] 글 신고 수 기준 충족: 대상유저={}, reportType={}, restrictionType={}", user, reportType.getType(), restrictionType.getContent());

        Status status = statusManager.getStatus(RESTRICTION, "PENDING");
        RestrictionTarget restriction = RestrictionTarget.builder()
                .user(user)
                .reportType(reportType)
                .restrictionType(restrictionType)
                .status(status)
                .build();
        restrictionTargetRepository.save(restriction);
        log.info("[applyRestriction] 제재 대상 등록 완료. id: {}, 제재타입: {}, status: {}", restriction.getRestrictionTargetId(), restrictionType.getContent(), status.getCode());

        List<ReportHistory> reportsToRestrict = reportHistoryRepository
                .findReportsToRestrict(user, reportType, acceptedStatuses);
        log.info("[applyRestriction] 제재와 연관된 신고내역 조회: 총 {}건", reportsToRestrict.size());

        for (ReportHistory report : reportsToRestrict) {
            report.updateRestrictionTarget(restriction);
        }
        reportHistoryRepository.saveAll(reportsToRestrict);
        log.info("[applyRestriction] 연관 신고내역에 제재 id 등록완료. restrictionTargetId: {}", restriction.getRestrictionTargetId());
    }

    @Override
    public ReportDetailResponseDto getReportDetail(Long reportId) {

        ReportDetailResponseDto response = reportHistoryRepository.getReportDetail(reportId);

        if(response == null){
            throw new ReportNotFoundException();
        }

        log.info("[getReportDetail] 신고상세 조회 완료 : {} ", response.getReportId());
        return response;
    }

    @Override
    public List<ReportTypeResponseDto> getReportTypes() {
        List<ReportType> reportTypes = reportTypeRepository.findAll();
        return reportTypes.stream()
                .map(ReportTypeResponseDto::from)
                .toList();
    }
}
