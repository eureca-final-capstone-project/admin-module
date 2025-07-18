package eureca.capstone.project.admin.report.service.impl;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.admin.common.exception.custom.*;
import eureca.capstone.project.admin.common.repository.StatusRepository;
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
    private final UserAuthorityRepository userAuthorityRepository;

    @Override
    public ReportCountDto getReportCounts() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long todayCount = reportHistoryRepository.countByCreatedAtAfter(startOfToday);
        long totalCount = reportHistoryRepository.count();

        log.info("[getReportCounts] todayCount: {}, totalCount: {}", todayCount, totalCount);
        return ReportCountDto.builder()
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
    public Page<RestrictionDto> getRestrictionListByStatusCode(String statusCode,String keyword, Pageable pageable) {
        Page<RestrictionTarget> restrictionTarget = restrictionTargetRepository.findByCriteria(statusCode, keyword, pageable);
        log.info("[getRestrictionListByStatusCode] 신고 내역 조회 (keyword: {}, statusCode: {}): 총 {} 건", keyword, statusCode, restrictionTarget.getTotalElements());
        return restrictionTarget.map(RestrictionDto::from);
    }


    @Override
    @Transactional
    public void processReportByAdmin(Long reportHistoryId, ProcessReportDto request) {
        ReportHistory reportHistory = reportHistoryRepository.findById(reportHistoryId)
                .orElseThrow(ReportNotFoundException::new);

        Status pendingStatus = statusManager.getStatus(REPORT,"PENDING");
        Status aiRejectStatus = statusManager.getStatus(REPORT,"AI_REJECTED");

        List<Status> processableStatus = List.of(pendingStatus, aiRejectStatus);
        if (!processableStatus.contains(reportHistory.getStatus())) {
            throw new AlreadyProcessedReportException();
        }

        if (!request.getApproved()) {
            Status adminRejectedStatus = statusManager.getStatus(REPORT, "ADMIN_REJECTED");
            reportHistory.updateStatus(adminRejectedStatus);

            log.info("[processReportByAdmin] 관리자 신고 거절. status={}", reportHistory.getStatus().getCode());
            return;
        }

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
            case 1 -> { // 욕설 및 비속어
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 욕설 및 비속어 글 신고 수 5회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType, acceptedStatuses);
                }
            }
            case 2 -> {  // 주제 불일치
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(4L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 주제 불일치 글 신고 수 5회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType, acceptedStatuses);
                }
            }
            case 3 -> { // 음란 내용 포함
                restrictionType = restrictionTypeRepository.findById(2L).orElseThrow(RestrictionTypeNotFoundException::new);
                log.info("[checkAndApplyRestriction] 음란 내용 포함 글 신고: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                applyRestriction(seller, reportType, restrictionType, acceptedStatuses);
            }
            case 4 -> { // 외부 채널 유도
                if (violationCount >= 3) {
                    restrictionType = restrictionTypeRepository.findById(3L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 외부 채널 유도 신고 수 3회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType, acceptedStatuses);
                }
            }
            case 5 -> { // 비방/저격 포함
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 비방/저격 글 신고 수 5회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType, acceptedStatuses);
                }
            }
        }
    }

    private void applyRestriction(User user, ReportType reportType, RestrictionType restrictionType, List<Status> acceptedStatuses) {
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

    @Transactional
    @Override
    public void expireRestrictions(List<Long> restrictionTargetIds) {
        if (restrictionTargetIds == null || restrictionTargetIds.isEmpty()) {
            log.info("[expireRestrictions] 제재만료 대상 없음");
            return;
        }
        Status expiredStatus = statusManager.getStatus(RESTRICTION, "RESTRICT_EXPIRATION");
        restrictionTargetRepository.updateStatusForIds(restrictionTargetIds, expiredStatus);

        log.info("[expireRestrictions] 제재만료처리 완료");
    }

    @Transactional
    @Override
    public void acceptRestrictions(Long restrictionTargetId) {
        RestrictionTarget restrictionTarget = restrictionTargetRepository.findById(restrictionTargetId)
                .orElseThrow(RestrictionTargetNotFoundException::new);

        Status completedStatus = statusManager.getStatus(RESTRICTION, "COMPLETED");

        if(restrictionTarget.getStatus().equals(completedStatus)) {
            throw new AlreadyProcessedRestrictionException();
        }

        User user = restrictionTarget.getUser();
        Integer duration = restrictionTarget.getRestrictionType().getDuration();

        log.info("[acceptRestrictions] 제재대상 및 기간 조회. 사용자: {}, 제재타입: {}, 제재기간: {} (-1은 영구정지)", user.getUserId(), restrictionTarget.getRestrictionType().getContent(), duration);

        // 제재 만료일
        LocalDateTime expiresAt;

        if (duration == -1) { // 영구정지일 경우 사용자 block
            expiresAt = null;
            user.updateUserStatus(statusManager.getStatus("USER", "BANNED"));
            log.info("[acceptRestrictions] 영구정지 user 상태 변경: {}", user.getStatus().getCode());
        }
        else{
            Authority authority = restrictionTarget.getRestrictionType().getAuthority();
            UserAuthority userAuthority = userAuthorityRepository.findByUserAndAuthority(user, authority);
            log.info("[acceptRestrictions] userAuthority에서 권한 조회결과: {}", userAuthority);

            // 해당 권한에 대한 제재내역이 없는 경우
            if(userAuthority == null){
                expiresAt = LocalDateTime.now().plusDays(duration);
                userAuthorityRepository.save(
                        UserAuthority.builder()
                            .user(user)
                            .authority(authority)
                            .expiredAt(expiresAt)
                            .build());
                log.info("[acceptRestrictions] 해당 권한에 대한 제재내역 없음 => expiresAt: {} ", expiresAt);
            }
            // 해당 권한에 대해 이미 제재받은 경우
            else{
                expiresAt = userAuthority.getExpiredAt().plusDays(duration);
                userAuthority.updateExpiresAt(expiresAt);
                log.info("[acceptRestrictions] 해당 권한에 대한 제재내역 있음 => expiresAt: {} ", expiresAt);
            }
        }
        restrictionTarget.updateExpiresAt(expiresAt);
        restrictionTarget.updateStatus(statusManager.getStatus("RESTRICTION", "COMPLETED"));
        log.info("[acceptRestrictions] 제재 완료. expiresAt: {}, status: {}", expiresAt, restrictionTarget.getStatus().getCode());

        //제재와 연관된 신고 내역들의 상태를 '제재 완료'로 변경
        Status reportCompletedStatus = statusManager.getStatus("REPORT", "COMPLETED");
        List<ReportHistory> relatedReports = reportHistoryRepository.findByRestrictionTarget(restrictionTarget);
        relatedReports.forEach(report -> report.updateStatus(reportCompletedStatus));
        reportHistoryRepository.saveAll(relatedReports);
        log.info("[acceptRestrictions] 연관된 신고 내역 {}건의 상태를 '제재 완료'로 변경했습니다.", relatedReports.size());
    }

    @Transactional
    @Override
    public void rejectRestrictions(Long restrictionTargetId) {
        RestrictionTarget restrictionTarget = restrictionTargetRepository.findById(restrictionTargetId)
                .orElseThrow(RestrictionTargetNotFoundException::new);

        Status rejectedStatus = statusManager.getStatus(RESTRICTION, "REJECTED");

        if(restrictionTarget.getStatus().equals(rejectedStatus)) {
            throw new AlreadyProcessedRestrictionException();
        }

        restrictionTarget.updateStatus(statusManager.getStatus("RESTRICTION", "REJECTED"));
        log.info("[acceptRestrictions] 제재 거절 완료. status: {}", restrictionTarget.getStatus().getCode());

        Status reportRejectedStatus = statusManager.getStatus("REPORT", "REJECTED");
        List<ReportHistory> relatedReports = reportHistoryRepository.findByRestrictionTarget(restrictionTarget);
        relatedReports.forEach(report -> report.updateStatus(reportRejectedStatus));
        reportHistoryRepository.saveAll(relatedReports);
        log.info("[rejectRestrictions] 연관된 신고 내역 {}건의 상태를 '제재 미승인'으로 변경했습니다.", relatedReports.size());
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
    public List<RestrictionReportResponseDto> getRestrictionReportHistory(Long restrictionId) {

        RestrictionTarget restriction = restrictionTargetRepository.findById(restrictionId)
                .orElseThrow(RestrictionTargetNotFoundException::new);

        List<RestrictionReportResponseDto> response = reportHistoryRepository.getRestrictionReportList(restrictionId);

        log.info("[getRestrictionReportHistory] 제재와 연관된 신고내역 조회: 총 {} 건", response.size());

        return response;
    }

    @Override
    public RestrictExpiredResponseDto getRestrictExpiredList() {
        Status completedStatus = statusManager.getStatus(RESTRICTION, "COMPLETED");

        List<RestrictionTarget> expiredTargets = restrictionTargetRepository.findExpiredRestrictions(
                LocalDateTime.now(),
                completedStatus
        );

        List<RestrictExpiredResponseDto.ExpiredRestrictionInfo> expiredInfoList = expiredTargets.stream()
                .map(RestrictExpiredResponseDto.ExpiredRestrictionInfo::from)
                .toList();

        log.info("[getRestrictExpiredList] 제재 만료 대상: {}건", expiredInfoList.size());
        return new RestrictExpiredResponseDto(expiredInfoList);
    }
}
