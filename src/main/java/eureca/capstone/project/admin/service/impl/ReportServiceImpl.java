package eureca.capstone.project.admin.service.impl;

import eureca.capstone.project.admin.domain.ReportHistory;
import eureca.capstone.project.admin.domain.ReportType;
import eureca.capstone.project.admin.domain.RestrictionTarget;
import eureca.capstone.project.admin.domain.RestrictionType;
import eureca.capstone.project.admin.domain.status.ReportHistoryStatus;
import eureca.capstone.project.admin.domain.status.RestrictionTargetStatus;
import eureca.capstone.project.admin.dto.request.AIReviewRequestDto;
import eureca.capstone.project.admin.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import eureca.capstone.project.admin.repository.ReportHistoryRepository;
import eureca.capstone.project.admin.repository.ReportTypeRepository;
import eureca.capstone.project.admin.repository.RestrictionTargetRepository;
import eureca.capstone.project.admin.repository.RestrictionTypeRepository;
import eureca.capstone.project.admin.service.ReportService;
import eureca.capstone.project.admin.service.external.AIReviewService;
import eureca.capstone.project.admin.service.external.TransactionModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

        // 오늘 신고 건수 및 전체 신고 건수 조회
        long todayCount = reportHistoryRepository.countByCreatedAtAfter(startOfToday);
        long totalCount = reportHistoryRepository.count();

        return ReportCountDto.builder()
                .todayReportCount(todayCount)
                .totalReportCount(totalCount)
                .build();
    }

    @Override
    public Page<ReportHistoryDto> getReportHistoryList(String status, Pageable pageable) {
        if (status == null || status.isBlank()) {
            return reportHistoryRepository.findAll(pageable).map(ReportHistoryDto::from);
        } else {
            return reportHistoryRepository.findByStatus(ReportHistoryStatus.from(status), pageable).map(ReportHistoryDto::from);
        }
    }

    @Override
    public Page<RestrictionDto> getRestrictionList(Pageable pageable) {
        return restrictionTargetRepository.findAll(pageable)
                .map(RestrictionDto::from);
    }

    @Override
    @Transactional
    public void processReportByAdmin(Long reportHistoryId, ProcessReportDto request) {
        ReportHistory reportHistory = reportHistoryRepository.findById(reportHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("신고내역을 찾지 못했습니다."));

        List<ReportHistoryStatus> processableStatus = List.of(ReportHistoryStatus.PENDING, ReportHistoryStatus.AI_REJECTED);
        if (!processableStatus.contains(reportHistory.getStatus())) {
            throw new IllegalStateException("처리할 수 없는 상태입니다. 현재 상태: " + reportHistory.getStatus());
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
                .orElseThrow(() -> new IllegalArgumentException("신고 유형을 찾을 수 없습니다."));

        // 1. transaction-module에서 게시글 정보를 비동기적으로 가져옵니다.
        // TODO: 트랜잭션 모듈에서 상세조회하는 API 구현 완료 시 transactionModuleService 수정하기
        transactionModuleService.getFeedDetails(transactionFeedId)
                .flatMap(feedDto -> {
                    // 2. (성공 시) 받아온 feedDto로 AI 검토 요청 DTO를 만듭니다.
                    AIReviewRequestDto requestDto = new AIReviewRequestDto(
                            feedDto.getTitle(),
                            feedDto.getContent(),
                            reason,
                            reportType.getType()
                    );
                    // 3. AI 서비스에 검토를 요청하는 또 다른 비동기 파이프라인을 반환합니다.
                    return aiReviewService.requestReview(requestDto);
                })
                .subscribe(aiResponse -> { // 4. (최종 성공 시) AI 응답으로 후속 처리를 합니다.
                    // AI 응답에 따라 신고 상태 결정
                    ReportHistoryStatus initialStatus;
                    if (aiResponse.getConfidence() < 0.8) {
                        initialStatus = ReportHistoryStatus.PENDING;
                    } else {
                        switch (aiResponse.getResult()) {
                            case "ACCEPT": initialStatus = ReportHistoryStatus.AI_ACCEPTED; break;
                            case "REJECT": initialStatus = ReportHistoryStatus.AI_REJECTED; break;
                            default: initialStatus = ReportHistoryStatus.PENDING; break;
                        }
                    }

                    // ReportHistory 생성 및 저장
                    ReportHistory newReport = ReportHistory.builder()
                            .userId(userId)
                            .transactionFeedId(transactionFeedId)
                            .reportType(reportType)
                            .reason(reason)
                            .status(initialStatus)
                            .isModerated(true)
                            .build();
                    reportHistoryRepository.save(newReport);

                    // AI가 승인한 경우, 즉시 제재 로직 실행
                    if (initialStatus == ReportHistoryStatus.AI_ACCEPTED) {
                        checkAndApplyRestriction(userId, reportType);
                    }
                });
    }

    private void checkAndApplyRestriction(Long userId, ReportType reportType) {
        // 승인된 신고(AI, 관리자) 목록
        List<ReportHistoryStatus> acceptedStatuses = List.of(ReportHistoryStatus.AI_ACCEPTED, ReportHistoryStatus.ADMIN_ACCEPTED);

        // 현재 승인된 건을 포함한 누적 위반 횟수 계산
        long violationCount = reportHistoryRepository.countByUserIdAndReportTypeAndStatusIn(userId, reportType, acceptedStatuses);

        // 제재 규칙 적용
        switch (reportType.getType()) {
            case "음란 내용 포함":
                // 즉시 영구 정지
                applyRestriction(userId, reportType, "영구 제한", null);
                break;

            case "욕설 및 비속어 포함":
                if (violationCount >= 5) {
                    applyRestriction(userId, reportType, "게시글 작성 제한", 7);
                }
                break;

            case "주제 불일치": // '주제 불일치' 유형으로 가정
                if (violationCount >= 5) {
                    applyRestriction(userId, reportType, "게시글 작성 제한(1일)", 1);
                }
                break;

            case "허위 신고":
                if (violationCount >= 3) {
                    applyRestriction(userId, reportType, "신고 제한", 30);
                }
                break;
        }
    }

    private void applyRestriction(Long userId, ReportType reportType, String restrictionContent, Integer duration) {
        // 제재 내용으로 제재 유형 조회
        RestrictionType restrictionType = restrictionTypeRepository.findByContent(restrictionContent)
                .orElseThrow(() -> new IllegalStateException(restrictionContent + " 유형의 제재를 찾을 수 없습니다."));

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
}
