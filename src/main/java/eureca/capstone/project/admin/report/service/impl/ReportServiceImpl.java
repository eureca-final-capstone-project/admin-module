package eureca.capstone.project.admin.report.service.impl;

import eureca.capstone.project.admin.auth.entity.Authority;
import eureca.capstone.project.admin.auth.entity.UserAuthority;
import eureca.capstone.project.admin.auth.repository.UserAuthorityRepository;
import eureca.capstone.project.admin.common.entity.StatusConst;
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
    private final StatusRepository statusRepository;
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
    public Page<ReportHistoryDto> getReportHistoryListByStatusCode(String statusCode, Pageable pageable) {
        if (statusCode == null || statusCode.isBlank()) {
            Page<ReportHistoryDto> response = reportHistoryRepository.findAll(pageable).map(ReportHistoryDto::from);
            log.info("[getReportHistoryListByStatusCode] 신고내역 전체 조회: {}건", response.getTotalElements());
            return response;
        }
        Status status = statusRepository.findByDomainAndCode(REPORT, statusCode)
                .orElseThrow(ReportTypeNotFoundException::new);
        Page<ReportHistoryDto> response = reportHistoryRepository.findByStatus(status, pageable).map(ReportHistoryDto::from);
        log.info("[getReportHistoryListByStatusCode] 신고내역 상태 필터링해서 조회: {}건. status = {}", response.getTotalElements(), statusCode);
        return response;
    }

    @Override
    public Page<RestrictionDto> getRestrictionListByStatusCode(String statusCode, Pageable pageable) {
        if (statusCode == null || statusCode.isBlank()) {
            Page<RestrictionDto> response = restrictionTargetRepository.findAll(pageable).map(RestrictionDto::from);
            log.info("[getRestrictionListByStatusCode] 제재내역 전체 조회: {}건", response.getTotalElements());
            return response;
        }
        Status status = statusRepository.findByDomainAndCode(RESTRICTION, statusCode)
                .orElseThrow(RestrictionTypeNotFoundException::new);
        Page<RestrictionDto> response = restrictionTargetRepository.findByStatus(status, pageable).map(RestrictionDto::from);
        log.info("[getRestrictionListByStatusCode] 제재내역 상태 필터링해서 조회: {}건. status = {}", response.getTotalElements(), statusCode);
        return response;
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

            log.info("[processReportByAdmin] 관리자 신고 거절. status={}", reportHistory.getStatus().getCode());
            return;
        }

        // 변경점: findByCode -> findByDomainAndCode
        Status adminAcceptedStatus = statusRepository.findByDomainAndCode(REPORT, "ADMIN_ACCEPTED")
                .orElseThrow(StatusNotFoundException::new);
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

        log.info("[createReportAndProcessWithAI] AI 판단 이후 신고내역 추가. 피신고자: {}", seller);

        // 변경점: findByCode -> findByDomainAndCode
        Status aiAcceptedStatus = statusRepository.findByDomainAndCode(REPORT, "AI_ACCEPTED")
                .orElseThrow(StatusNotFoundException::new);
        if (initialStatus.equals(aiAcceptedStatus)) {
            log.info("[createReportAndProcessWithAI] AI 승인 완료");
            checkAndApplyRestriction(seller, reportType);
        }
    }


    private Status getReportHistoryStatus(AIReviewResponseDto aiResponse) {
        if (aiResponse.getConfidence() < 0.8) {
            log.info("[getReportHistoryStatus] 신고내용 AI 판단 모호 (신뢰도 : {})", aiResponse.getConfidence());
            return statusRepository.findByDomainAndCode(REPORT,"PENDING")
                    .orElseThrow(StatusNotFoundException::new);
        } else {
            // 변경점: findByCode -> findByDomainAndCode
            return switch (aiResponse.getResult()) {
                case "ACCEPT" -> statusRepository.findByDomainAndCode(REPORT, "AI_ACCEPTED").orElseThrow(StatusNotFoundException::new);
                case "REJECT" -> statusRepository.findByDomainAndCode(REPORT, "AI_REJECTED").orElseThrow(StatusNotFoundException::new);
                default -> statusRepository.findByDomainAndCode(REPORT,"PENDING").orElseThrow(StatusNotFoundException::new);
            };
        }
    }

    private void checkAndApplyRestriction(User seller, ReportType reportType) {
        Status aiAcceptStatus = statusRepository.findByDomainAndCode(REPORT, "AI_ACCEPTED").orElseThrow(StatusNotFoundException::new);
        Status adminAcceptStatus = statusRepository.findByDomainAndCode(REPORT, "ADMIN_ACCEPTED").orElseThrow(StatusNotFoundException::new);
        List<Status> acceptedStatuses = List.of(aiAcceptStatus, adminAcceptStatus);

        long violationCount = reportHistoryRepository.countBySellerAndReportTypeAndStatusIn(seller, reportType, acceptedStatuses);
        RestrictionType restrictionType;
        log.info("[checkAndApplyRestriction] 피신고 수={}", violationCount);

        switch (reportType.getReportTypeId().intValue()) {
            case 1 -> { // 욕설 및 비속어
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 욕설 및 비속어 글 신고 수 5회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType);
                }
            }
            case 2 -> {  // 주제 불일치
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(4L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 주제 불일치 글 신고 수 5회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType);
                }
            }
            case 3 -> { // 음란 내용 포함
                restrictionType = restrictionTypeRepository.findById(2L).orElseThrow(RestrictionTypeNotFoundException::new);
                log.info("[checkAndApplyRestriction] 음란 내용 포함 글 신고: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                applyRestriction(seller, reportType, restrictionType);
            }
            case 4 -> { // 외부 채널 유도
                if (violationCount >= 3) {
                    restrictionType = restrictionTypeRepository.findById(3L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 외부 채널 유도 신고 수 3회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType);
                }
            }
            case 5 -> { // 중복 게시글
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 중복 게시글 신고 수 5회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType);
                }
            }
            case 6 -> { // 비방/저격 포함
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L).orElseThrow(RestrictionTypeNotFoundException::new);
                    log.info("[checkAndApplyRestriction] 비방/저격 글 신고 수 5회 이상: 대상유저={}, restrictionType={}", seller, restrictionType.getContent());
                    applyRestriction(seller, reportType, restrictionType);
                }
            }
        }
    }

    private void applyRestriction(User user, ReportType reportType, RestrictionType restrictionType) {
        Status status = statusRepository.findByDomainAndCode(RESTRICTION, "PENDING")
                .orElseThrow(StatusNotFoundException::new);
        RestrictionTarget restriction = RestrictionTarget.builder()
                .user(user)
                .reportType(reportType)
                .restrictionType(restrictionType)
                .status(status)
                .build();
        restrictionTargetRepository.save(restriction);

        log.info("[applyRestriction] 제재 적용. id: {}, 제재타입: {}, status: {}", restriction.getRestrictionTargetId(), restrictionType.getContent(), status.getCode());
    }

    @Transactional
    @Override
    public void expireRestrictions(List<Long> restrictionTargetIds) {
        if (restrictionTargetIds == null || restrictionTargetIds.isEmpty()) {
            log.info("[expireRestrictions] 제재만료 대상 없음");
            return;
        }
        Status expiredStatus = statusRepository.findByDomainAndCode(RESTRICTION, "RESTRICT_EXPIRATION")
                .orElseThrow(StatusNotFoundException::new);
        restrictionTargetRepository.updateStatusForIds(restrictionTargetIds, expiredStatus);

        log.info("[expireRestrictions] 제재만료처리 완료");
    }

    @Transactional
    @Override
    public void acceptRestrictions(Long restrictionTargetId) {
        RestrictionTarget restrictionTarget = restrictionTargetRepository.findById(restrictionTargetId)
                .orElseThrow(RestrictionTargetNotFoundException::new);
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
    }

    @Transactional
    @Override
    public void rejectRestrictions(Long restrictionTargetId) {
        RestrictionTarget restrictionTarget = restrictionTargetRepository.findById(restrictionTargetId)
                .orElseThrow(RestrictionTargetNotFoundException::new);

        restrictionTarget.updateStatus(statusManager.getStatus("RESTRICTION", "REJECTED"));
        log.info("[acceptRestrictions] 제재 거절 완료. status: {}", restrictionTarget.getStatus().getCode());
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

        log.info("[getRestrictExpiredList] 제재 만료 대상: {}건", expiredInfoList.size());
        return new RestrictExpiredResponseDto(expiredInfoList);
    }
}
