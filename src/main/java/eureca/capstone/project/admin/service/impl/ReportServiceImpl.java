package eureca.capstone.project.admin.service.impl;

import eureca.capstone.project.admin.domain.ReportHistory;
import eureca.capstone.project.admin.domain.ReportType;
import eureca.capstone.project.admin.domain.RestrictionTarget;
import eureca.capstone.project.admin.domain.RestrictionType;
import eureca.capstone.project.admin.domain.common.entity.Status;
import eureca.capstone.project.admin.domain.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.domain.user.entity.User;
import eureca.capstone.project.admin.dto.request.AIReviewRequestDto;
import eureca.capstone.project.admin.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.dto.response.*;
import eureca.capstone.project.admin.exception.*;
import eureca.capstone.project.admin.repository.*;
import eureca.capstone.project.admin.service.ReportService;
import eureca.capstone.project.admin.service.external.AIReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private final StatusRepository statusRepository;
    private final AIReviewService aiReviewService;

    @Override
    public ReportCountDto getReportCounts() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // 오늘 신고 건수 및 전체 신고 건수 조회
        Long todayCount = reportHistoryRepository.countByCreatedAtAfter(startOfToday);
        long totalCount = reportHistoryRepository.count();

        return ReportCountDto.builder()
                .todayReportCount(todayCount)
                .totalReportCount(totalCount)
                .build();
    }

    @Override
    public Page<ReportHistoryDto> getReportHistoryList(Status status, Pageable pageable) {
        if (status == null) {
            return reportHistoryRepository.findAll(pageable).map(ReportHistoryDto::from);
        } else {
            return reportHistoryRepository.findByStatus(status, pageable).map(ReportHistoryDto::from);
        }
    }

    @Override
    public Page<RestrictionDto> getRestrictionList(Status status, Pageable pageable) {
        if (status == null) {
            return restrictionTargetRepository.findAll(pageable).map(RestrictionDto::from);
        } else {
            return restrictionTargetRepository.findByStatus(status, pageable).map(RestrictionDto::from);
        }
    }

    @Override
    @Transactional
    public void processReportByAdmin(Long reportHistoryId, ProcessReportDto request) {
        ReportHistory reportHistory = reportHistoryRepository.findById(reportHistoryId)
                .orElseThrow(ReportNotFoundException::new);

        Status pendingStatus = statusRepository.findByCode("MODERATION_PENDING");
        Status aiRejectStatus = statusRepository.findByCode("AI_REJECTED");

        List<Status> processableStatus = List.of(pendingStatus, aiRejectStatus);
        if (!processableStatus.contains(reportHistory.getStatus())) {
            throw new AlreadyProcessedReportException();
        }

        if (!request.getApproved()) {
            reportHistory.updateStatus(statusRepository.findByCode("ADMIN_REJECTED"));
            return;
        }

        reportHistory.updateStatus(statusRepository.findByCode("ADMIN_ACCEPTED"));

        checkAndApplyRestriction(reportHistory.getUser(), reportHistory.getReportType());
    }

    @Override
    @Transactional
    public void createReportAndProcessWithAI(Long userId, Long transactionFeedId, Long reportTypeId, String reason) {

        ReportType reportType = reportTypeRepository.findById(reportTypeId)
                .orElseThrow(ReportTypeNotFoundException::new);

        User user = userRepository.findById(userId)
                        .orElseThrow(IllegalArgumentException::new);

        TransactionFeed transactionFeed = transactionFeedRepository.findById(transactionFeedId)
                        .orElseThrow(IllegalArgumentException::new);

        if(reportHistoryRepository.existsByUserAndSeller(user, transactionFeed.getUser())){
            throw new DuplicateReportException();
        }

        AIReviewRequestDto requestDto = new AIReviewRequestDto(
                transactionFeed.getTitle(),
                transactionFeed.getContent(),
                reason,
                reportType.getType()
        );

        AIReviewResponseDto aiResponse = aiReviewService.requestReview(requestDto);

        Status initialStatus = getReportHistoryStatus(aiResponse);

        ReportHistory newReport = ReportHistory.builder()
                .user(user)
                .transactionFeed(transactionFeed)
                .seller(transactionFeed.getUser())
                .reportType(reportType)
                .reason(reason)
                .isModerated(true)
                .status(initialStatus)
                .build();
        reportHistoryRepository.save(newReport);

        if (initialStatus == statusRepository.findByCode("AI_ACCEPTED")) {
            checkAndApplyRestriction(user, reportType);
        }
    }


    private Status getReportHistoryStatus(AIReviewResponseDto aiResponse) {
        Status initialStatus = null; // AI 응답에 따라 상태 결정
        if (aiResponse.getConfidence() < 0.8) {
            initialStatus = statusRepository.findByCode("MODERATION_PENDING");
        } else {
            initialStatus = switch (aiResponse.getResult()) {
                case "ACCEPT" -> statusRepository.findByCode("AI_ACCEPTED");
                case "REJECT" -> statusRepository.findByCode("AI_REJECTED");
                default -> statusRepository.findByCode("MODERATION_PENDING");
            };
        }
        return initialStatus;
    }

    private void checkAndApplyRestriction(User user, ReportType reportType) {
        // 승인된 신고(AI, 관리자) 목록
        Status aiAcceptStatus = statusRepository.findByCode("AI_ACCEPTED");
        Status adminAcceptStatus = statusRepository.findByCode("ADMIN_ACCEPTED");
        List<Status> acceptedStatuses = List.of(aiAcceptStatus, adminAcceptStatus);

        // 현재 승인된 건을 포함한 누적 위반 횟수 계산
        long violationCount = reportHistoryRepository.countByUserAndReportTypeAndStatusIn(user, reportType, acceptedStatuses);
        RestrictionType restrictionType = null;

        switch (reportType.getReportTypeId().intValue()) {
            case 3: // 음란 내용 포함
                restrictionType = restrictionTypeRepository.findById(2L)
                                .orElseThrow(ReportTypeNotFoundException::new);
                applyRestriction(user, reportType, restrictionType.getContent(), null);
                break;

            case 1: // 욕설 및 비속어 포함
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L)
                            .orElseThrow(ReportTypeNotFoundException::new);
                    applyRestriction(user, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;

            case 2: // 주제 관련 없음
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(4L)
                            .orElseThrow(ReportTypeNotFoundException::new);
                    applyRestriction(user, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;

            case 4: // 허위신고
                if (violationCount >= 3) {
                    restrictionType = restrictionTypeRepository.findById(3L)
                            .orElseThrow(ReportTypeNotFoundException::new);
                    applyRestriction(user, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;
        }
    }

    private void applyRestriction(User user, ReportType reportType, String restrictionContent, Integer duration) {
        // 제재 내용으로 제재 유형 조회
        RestrictionType restrictionType = restrictionTypeRepository.findByContent(restrictionContent)
                .orElseThrow(RestrictionTypeNotFoundException::new);

        // 제재 만료일 계산
        LocalDateTime expiresAt = (duration == null) ? null : LocalDateTime.now().plusDays(duration);

        // 제재 대상 생성 및 저장
        RestrictionTarget restriction = RestrictionTarget.builder()
                .user(user)
                .reportType(reportType)
                .restrictionType(restrictionType)
                .status(statusRepository.findByCode("PENDING"))
                .expiresAt(expiresAt)
                .build();

        restrictionTargetRepository.save(restriction);
    }

    @Transactional
    @Override
    public void expireRestrictions(List<Long> restrictionTargetIds) {
        if (restrictionTargetIds == null || restrictionTargetIds.isEmpty()) {
            return;
        }
        restrictionTargetRepository.updateStatusForIds(
                restrictionTargetIds,
                statusRepository.findByCode("RESTRICT_EXPIRATION")
        );
    }

    @Override
    public RestrictExpiredResponseDto getRestrictExpiredList() {
        // 1. 현재 시간을 기준으로 만료된 'ACCEPTED' 상태의 제재 기록을 조회
        List<RestrictionTarget> expiredTargets = restrictionTargetRepository.findExpiredRestrictions(
                LocalDateTime.now(),
                statusRepository.findByCode("COMPLETED")
        );

        // 2. 각 제재 기록을 ExpiredRestrictionInfo DTO로 변환
        List<RestrictExpiredResponseDto.ExpiredRestrictionInfo> expiredInfoList = expiredTargets.stream()
                .map(RestrictExpiredResponseDto.ExpiredRestrictionInfo::from)
                .toList();

        // 3. 최종 DTO에 담아 반환
        return new RestrictExpiredResponseDto(expiredInfoList);
    }
}
