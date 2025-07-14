package eureca.capstone.project.admin.service.impl;

import eureca.capstone.project.admin.domain.ReportHistory;
import eureca.capstone.project.admin.domain.ReportType;
import eureca.capstone.project.admin.domain.RestrictionTarget;
import eureca.capstone.project.admin.domain.RestrictionType;
import eureca.capstone.project.admin.domain.status.ReportHistoryStatus;
import eureca.capstone.project.admin.domain.status.RestrictionTargetStatus;
import eureca.capstone.project.admin.dto.request.AIReviewRequestDto;
import eureca.capstone.project.admin.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.dto.response.*;
import eureca.capstone.project.admin.exception.*;
import eureca.capstone.project.admin.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.repository.ReportTypeRepository;
import eureca.capstone.project.admin.repository.RestrictionTargetRepository;
import eureca.capstone.project.admin.repository.RestrictionTypeRepository;
import eureca.capstone.project.admin.response.ErrorMessages;
import eureca.capstone.project.admin.service.ReportService;
import eureca.capstone.project.admin.service.external.AIReviewService;
import eureca.capstone.project.admin.service.external.TransactionModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final TransactionModuleService transactionModuleService;
    private final AIReviewService aiReviewService;

    @Override
    public ReportCountDto getReportCounts() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // 오늘 신고 건수 및 전체 신고 건수 조회
        Long todayCount = reportHistoryRepository.countByCreatedAtAfter(startOfToday);
        Long totalCount = reportHistoryRepository.count();

        return ReportCountDto.builder()
                .todayReportCount(todayCount)
                .totalReportCount(totalCount)
                .build();
    }

    @Override
    public Page<ReportHistoryDto> getReportHistoryList(String status, Pageable pageable) {
        if (!StringUtils.hasText(status)) {
            return reportHistoryRepository.findAll(pageable).map(ReportHistoryDto::from);
        } else {
            return reportHistoryRepository.findByStatus(ReportHistoryStatus.from(status), pageable).map(ReportHistoryDto::from);
        }
    }

    @Override
    public Page<RestrictionDto> getRestrictionList(String status, Pageable pageable) {
        if (!StringUtils.hasText(status)) {
            return restrictionTargetRepository.findAll(pageable).map(RestrictionDto::from);
        } else {
            return restrictionTargetRepository.findByStatus(RestrictionTargetStatus.from(status), pageable).map(RestrictionDto::from);
        }
    }

    @Override
    @Transactional
    public void processReportByAdmin(Long reportHistoryId, ProcessReportDto request) {
        ReportHistory reportHistory = reportHistoryRepository.findById(reportHistoryId)
                .orElseThrow(ReportNotFoundException::new);

        List<ReportHistoryStatus> processableStatus = List.of(ReportHistoryStatus.PENDING, ReportHistoryStatus.AI_REJECTED);

        if (!processableStatus.contains(reportHistory.getStatus())) {
            throw new AlreadyProcessedReportException();
        }

        if (!request.getApproved()) {
            reportHistory.rejectByAdmin();
            return;
        }

        reportHistory.approveByAdmin();

        checkAndApplyRestriction(reportHistory.getUserId(), reportHistory.getReportType());
    }

    @Override
    @Transactional
    public void createReportAndProcessWithAI(Long userId, Long transactionFeedId, Long reportTypeId, String reason) {

        ReportType reportType = reportTypeRepository.findById(reportTypeId)
                .orElseThrow(ReportTypeNotFoundException::new);

        // 1. transaction-module에서 게시글 정보를 비동기적으로 가져옵니다.
        // TODO: 트랜잭션 모듈에서 상세조회하는 API 구현 완료 시 transactionModuleService 수정하기
        transactionModuleService.getFeedDetails(transactionFeedId)
                .flatMap(feedDto -> {
                    // 2. 받아온 sellerId로 중복 신고 여부를 바로 확인합니다.
                    if (reportHistoryRepository.existsByUserIdAndSellerId(userId, feedDto.getSellerId())) {
                        return Mono.error(new DuplicateReportException());
                    }
                    // 2. (성공 시) 받아온 feedDto로 AI 검토 요청 DTO를 만듭니다.
                    AIReviewRequestDto requestDto = new AIReviewRequestDto(
                            feedDto.getTitle(),
                            feedDto.getContent(),
                            reason,
                            reportType.getType()
                    );
                    // 3. AI 서비스에 검토를 요청하는 또 다른 비동기 파이프라인을 반환합니다.
                    return aiReviewService.requestReview(requestDto)
                            .map(aiResponse -> Tuples.of(aiResponse, feedDto)); // AI 응답과 feedDto를 함께 전달
                })
                .doOnError(error -> {
                    if (!(error instanceof CustomException)) {
                        throw new AiReviewException();
                    }
                })
                .subscribe(
                        tuple -> { // 4. 최종적으로 ReportHistory 저장 시 sellerId를 함께 저장합니다.
                            var aiResponse = tuple.getT1();
                            var feedDto = tuple.getT2();

                            ReportHistoryStatus initialStatus = getReportHistoryStatus(aiResponse);

                            ReportHistory newReport = ReportHistory.builder()
                                    .userId(userId)
                                    .transactionFeedId(transactionFeedId)
                                    .sellerId(feedDto.getSellerId())
                                    .reportType(reportType)
                                    .reason(reason)
                                    .isModerated(true)
                                    .status(initialStatus)
                                    .build();
                            reportHistoryRepository.save(newReport);

                            if (initialStatus == ReportHistoryStatus.AI_ACCEPTED) {
                                checkAndApplyRestriction(userId, reportType);
                            }
                        },
                        error -> log.error("비동기 신고 처리 중 최종 에러 발생: {}", error.getMessage())
                );
    }


    private static ReportHistoryStatus getReportHistoryStatus(AIReviewResponseDto aiResponse) {
        ReportHistoryStatus initialStatus; // AI 응답에 따라 상태 결정
        if (aiResponse.getConfidence() < 0.8) {
            initialStatus = ReportHistoryStatus.PENDING;
        } else {
            initialStatus = switch (aiResponse.getResult()) {
                case "ACCEPT" -> ReportHistoryStatus.AI_ACCEPTED;
                case "REJECT" -> ReportHistoryStatus.AI_REJECTED;
                default -> ReportHistoryStatus.PENDING;
            };
        }
        return initialStatus;
    }

    private void checkAndApplyRestriction(Long userId, ReportType reportType) {
        List<ReportHistoryStatus> acceptedStatuses = List.of(ReportHistoryStatus.AI_ACCEPTED, ReportHistoryStatus.ADMIN_ACCEPTED);

        long violationCount = reportHistoryRepository.countByUserIdAndReportTypeAndStatusIn(userId, reportType, acceptedStatuses);
        RestrictionType restrictionType = null;

        switch (reportType.getReportTypeId().intValue()) {
            case 3: // 음란 내용 포함
                restrictionType = restrictionTypeRepository.findById(2L)
                                .orElseThrow(ReportTypeNotFoundException::new);
                applyRestriction(userId, reportType, restrictionType.getContent(), null);
                break;

            case 1: // 욕설 및 비속어 포함
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(1L)
                            .orElseThrow(ReportTypeNotFoundException::new);
                    applyRestriction(userId, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;

            case 2: // 주제 관련 없음
                if (violationCount >= 5) {
                    restrictionType = restrictionTypeRepository.findById(4L)
                            .orElseThrow(ReportTypeNotFoundException::new);
                    applyRestriction(userId, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;

            case 4: // 허위신고
                if (violationCount >= 3) {
                    restrictionType = restrictionTypeRepository.findById(3L)
                            .orElseThrow(ReportTypeNotFoundException::new);
                    applyRestriction(userId, reportType, restrictionType.getContent(), restrictionType.getDuration());
                }
                break;
        }
    }

    private void applyRestriction(Long userId, ReportType reportType, String restrictionContent, Integer duration) {
        // 제재 내용으로 제재 유형 조회
        RestrictionType restrictionType = restrictionTypeRepository.findByContent(restrictionContent)
                .orElseThrow(RestrictionTypeNotFoundException::new);

        // 제재 만료일 계산
        LocalDateTime expiresAt = (duration == null) ? null : LocalDateTime.now().plusDays(duration);

        // 제재 대상 생성 및 저장
        RestrictionTarget restriction = RestrictionTarget.builder()
                .userId(userId)
                .reportType(reportType)
                .restrictionType(restrictionType)
                .status(RestrictionTargetStatus.PENDING)
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
                RestrictionTargetStatus.EXPIRED
        );
    }

    @Override
    public RestrictExpiredResponseDto getRestrictExpiredList() {
        // 1. 현재 시간을 기준으로 만료된 'ACCEPTED' 상태의 제재 기록을 조회
        List<RestrictionTarget> expiredTargets = restrictionTargetRepository.findExpiredRestrictions(
                LocalDateTime.now(),
                RestrictionTargetStatus.ACCEPTED
        );

        // 2. 각 제재 기록을 ExpiredRestrictionInfo DTO로 변환
        List<RestrictExpiredResponseDto.ExpiredRestrictionInfo> expiredInfoList = expiredTargets.stream()
                .map(RestrictExpiredResponseDto.ExpiredRestrictionInfo::from)
                .toList();

        // 3. 최종 DTO에 담아 반환
        return new RestrictExpiredResponseDto(expiredInfoList);
    }
}
