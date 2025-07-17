package eureca.capstone.project.admin.report.service.impl;

import eureca.capstone.project.admin.common.entity.StatusConst;
import eureca.capstone.project.admin.common.exception.custom.*;
import eureca.capstone.project.admin.common.repository.StatusRepository;
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
    private final StatusRepository statusRepository;
    private final AIReviewService aiReviewService;

    @Override
    public ReportCountDto getReportCounts() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long todayCount = reportHistoryRepository.countByCreatedAtAfter(startOfToday);
        long totalCount = reportHistoryRepository.count();
        return ReportCountDto.builder()
                .todayReportCount(todayCount)
                .totalReportCount(totalCount)
                .build();
    }

    @Override
    public Page<ReportHistoryDto> getReportHistoryListByStatusCode(String statusCode, Pageable pageable) {
        if (statusCode == null || statusCode.isBlank()) {
            return reportHistoryRepository.findAll(pageable).map(ReportHistoryDto::from);
        }
        Status status = statusRepository.findByDomainAndCode(REPORT, statusCode)
                .orElseThrow(ReportTypeNotFoundException::new);
        return reportHistoryRepository.findByStatus(status, pageable).map(ReportHistoryDto::from);
    }

    @Override
    public Page<RestrictionDto> getRestrictionListByStatusCode(String statusCode, Pageable pageable) {
        if (statusCode == null || statusCode.isBlank()) {
            return restrictionTargetRepository.findAll(pageable).map(RestrictionDto::from);
        }
        Status status = statusRepository.findByDomainAndCode(RESTRICTION, statusCode)
                .orElseThrow(RestrictionTypeNotFoundException::new);
        return restrictionTargetRepository.findByStatus(status, pageable).map(RestrictionDto::from);
    }


    @Override
    @Transactional
    public void processReportByAdmin(Long reportHistoryId, ProcessReportDto request) {
        ReportHistory reportHistory = reportHistoryRepository.findById(reportHistoryId)
                .orElseThrow(ReportNotFoundException::new);

        Status pendingStatus = statusRepository.findByDomainAndCode(REPORT,"PENDING")
                .orElseThrow(StatusNotFoundException::new);
        Status aiRejectStatus = statusRepository.findByDomainAndCode(REPORT,"AI_REJECTED")
                .orElseThrow(StatusNotFoundException::new);

        List<Status> processableStatus = List.of(pendingStatus, aiRejectStatus);
        if (!processableStatus.contains(reportHistory.getStatus())) {
            throw new AlreadyProcessedReportException();
        }

        if (!request.getApproved()) {
            // 변경점: findByCode -> findByDomainAndCode
            Status adminRejectedStatus = statusRepository.findByDomainAndCode(REPORT, "ADMIN_REJECTED")
                    .orElseThrow(StatusNotFoundException::new);
            reportHistory.updateStatus(adminRejectedStatus);
            return;
        }

        // 변경점: findByCode -> findByDomainAndCode
        Status adminAcceptedStatus = statusRepository.findByDomainAndCode(REPORT, "ADMIN_ACCEPTED")
                .orElseThrow(StatusNotFoundException::new);
        reportHistory.updateStatus(adminAcceptedStatus);

        checkAndApplyRestriction(reportHistory.getUser(), reportHistory.getReportType());
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

        // 변경점: findByCode -> findByDomainAndCode
        Status aiAcceptedStatus = statusRepository.findByDomainAndCode(REPORT, "AI_ACCEPTED")
                .orElseThrow(StatusNotFoundException::new);
        if (initialStatus.equals(aiAcceptedStatus)) {
            checkAndApplyRestriction(user, reportType);
        }
    }


    private Status getReportHistoryStatus(AIReviewResponseDto aiResponse) {
        if (aiResponse.getConfidence() < 0.8) {
            return statusRepository.findByDomainAndCode(REPORT,"PENDING")
                    .orElseThrow(StatusNotFoundException::new);
        } else {
            return switch (aiResponse.getResult()) {
                case "ACCEPT" -> statusRepository.findByDomainAndCode(REPORT, "AI_ACCEPTED").orElseThrow(StatusNotFoundException::new);
                case "REJECT" -> statusRepository.findByDomainAndCode(REPORT, "AI_REJECTED").orElseThrow(StatusNotFoundException::new);
                default -> statusRepository.findByDomainAndCode(REPORT,"PENDING").orElseThrow(StatusNotFoundException::new);
            };
        }
    }

    private void checkAndApplyRestriction(User user, ReportType reportType) {
        Status aiAcceptStatus = statusRepository.findByDomainAndCode(REPORT, "AI_ACCEPTED").orElseThrow(StatusNotFoundException::new);
        Status adminAcceptStatus = statusRepository.findByDomainAndCode(REPORT, "ADMIN_ACCEPTED").orElseThrow(StatusNotFoundException::new);
        List<Status> acceptedStatuses = List.of(aiAcceptStatus, adminAcceptStatus);

        long violationCount = reportHistoryRepository.countByUserAndReportTypeAndStatusIn(user, reportType, acceptedStatuses);
        RestrictionType restrictionType;

        switch (reportType.getReportTypeId().intValue()) {
            case 1:
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L).orElseThrow(RestrictionTypeNotFoundException::new);
                    applyRestriction(user, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;
            case 2:
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(4L).orElseThrow(RestrictionTypeNotFoundException::new);
                    applyRestriction(user, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;
            case 3:
                restrictionType = restrictionTypeRepository.findById(2L).orElseThrow(RestrictionTypeNotFoundException::new);
                applyRestriction(user, reportType, restrictionType.getContent(), null);
                break;
            case 4:
                if (violationCount >= 3) {
                    restrictionType = restrictionTypeRepository.findById(3L).orElseThrow(RestrictionTypeNotFoundException::new);
                    applyRestriction(user, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;
        }
    }

    private void applyRestriction(User user, ReportType reportType, String restrictionContent, Integer duration) {
        RestrictionType restrictionType = restrictionTypeRepository.findByContent(restrictionContent)
                .orElseThrow(RestrictionTypeNotFoundException::new);
        LocalDateTime expiresAt = (duration == null) ? null : LocalDateTime.now().plusDays(duration);
        Status status = statusRepository.findByDomainAndCode(RESTRICTION, "PENDING")
                .orElseThrow(StatusNotFoundException::new);
        RestrictionTarget restriction = RestrictionTarget.builder()
                .user(user)
                .reportType(reportType)
                .restrictionType(restrictionType)
                .status(status)
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
        Status expiredStatus = statusRepository.findByDomainAndCode(RESTRICTION, "RESTRICT_EXPIRATION")
                .orElseThrow(StatusNotFoundException::new);
        restrictionTargetRepository.updateStatusForIds(restrictionTargetIds, expiredStatus);
    }

    @Override
    public RestrictExpiredResponseDto getRestrictExpiredList() {
        Status completedStatus = statusRepository.findByDomainAndCode(RESTRICTION, "COMPLETED")
                .orElseThrow(StatusNotFoundException::new);

        List<RestrictionTarget> expiredTargets = restrictionTargetRepository.findExpiredRestrictions(
                LocalDateTime.now(),
                completedStatus
        );
        List<RestrictExpiredResponseDto.ExpiredRestrictionInfo> expiredInfoList = expiredTargets.stream()
                .map(RestrictExpiredResponseDto.ExpiredRestrictionInfo::from)
                .toList();
        return new RestrictExpiredResponseDto(expiredInfoList);
    }
}
