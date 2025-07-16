package eureca.capstone.project.admin.report.controller;

import eureca.capstone.project.admin.report.dto.request.CreateReportRequestDto;
import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.report.dto.request.UpdateRestrictionStatusRequestDto;
import eureca.capstone.project.admin.report.dto.response.ReportCountDto;
import eureca.capstone.project.admin.report.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.report.dto.response.RestrictExpiredResponseDto;
import eureca.capstone.project.admin.report.dto.response.RestrictionDto;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고/제재 API", description = "사용자 신고 접수 및 관리자 기능 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 건수 조회", description = "오늘 및 전체 신고 건수를 조회합니다.")
    @GetMapping("/reports/counts")
    public BaseResponseDto<ReportCountDto> getReportCounts() {
        return BaseResponseDto.success(reportService.getReportCounts());
    }

    @Operation(summary = "신고 내역 목록 조회", description = "신고 내역을 페이징하여 조회합니다. status 파라미터로 필터링할 수 있습니다.")
    @GetMapping("/reports/history")
    public BaseResponseDto<Page<ReportHistoryDto>> getReportHistoryList(
            @Parameter(description = "필터링할 신고 상태 (예: PENDING, AI_ACCEPTED 등)") @RequestParam(required = false) String statusCode,
            Pageable pageable) {
        return BaseResponseDto.success(reportService.getReportHistoryListByStatusCode(statusCode, pageable));
    }

    @Operation(summary = "제재 내역 목록 조회", description = "제재 대상 내역을 페이징하여 조회합니다. status 파라미터로 필터링할 수 있습니다.")
    @GetMapping("/restrictions")
    public BaseResponseDto<Page<RestrictionDto>> getRestrictionList(
            @Parameter(description = "필터링할 신고 상태 (예: PENDING, ACCEPTED 등)") @RequestParam(required = false) String statusCode,
            Pageable pageable) {
        return BaseResponseDto.success(reportService.getRestrictionListByStatusCode(statusCode, pageable));
    }

    @Operation(summary = "게시글 신고 접수", description = "사용자가 게시글을 신고하면 AI가 1차 검토 후 접수합니다.")
    @PostMapping("/reports")
    public BaseResponseDto<Void> createReport(@RequestBody CreateReportRequestDto request) {
        reportService.createReportAndProcessWithAI(
                request.getUserId(),
                request.getTransactionFeedId(),
                request.getReportTypeId(),
                request.getReason()
        );
        return BaseResponseDto.success(null); // 데이터가 없는 성공 응답
    }

    @Operation(summary = "관리자 신고 처리", description = "관리자가 특정 신고를 승인 또는 거절 처리합니다.")
    @PatchMapping("/reports/history/{reportHistoryId}/process")
    public BaseResponseDto<Void> processReportByAdmin(
            @Parameter(description = "처리할 신고 ID") @PathVariable("reportHistoryId") Long reportHistoryId,
            @RequestBody ProcessReportDto request) {
        reportService.processReportByAdmin(reportHistoryId, request);
        return BaseResponseDto.success(null);
    }

    @Operation(summary = "만료된 제재 목록 조회", description = "배치 서버가 제재를 해제하기 위해 제재 기간이 만료된 사용자 목록을 조회합니다.")
    @GetMapping("/restrict-expired-list")
    public BaseResponseDto<RestrictExpiredResponseDto> restrictExpiredList() {
        return BaseResponseDto.success(reportService.getRestrictExpiredList());
    }

    @Operation(summary = "제재 상태 일괄 만료 처리", description = "배치 서버가 제재 해제 처리 후, 제재 기록의 상태를 EXPIRED로 변경합니다.")
    @PostMapping("/restrict-expired")
    public BaseResponseDto<Void> expireRestrictions(@RequestBody UpdateRestrictionStatusRequestDto request) {
        reportService.expireRestrictions(request.getRestrictionTargetIds());
        return BaseResponseDto.success(null);
    }
}