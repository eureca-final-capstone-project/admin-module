package eureca.capstone.project.admin.controller;

import eureca.capstone.project.admin.domain.status.ReportHistoryStatus;
import eureca.capstone.project.admin.dto.request.CreateReportRequestDto;
import eureca.capstone.project.admin.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.dto.request.UpdateRestrictionStatusRequestDto;
import eureca.capstone.project.admin.dto.response.ReportCountDto;
import eureca.capstone.project.admin.dto.response.ReportHistoryDto;
import eureca.capstone.project.admin.dto.response.RestrictExpiredResponseDto;
import eureca.capstone.project.admin.dto.response.RestrictionDto;
import eureca.capstone.project.admin.response.ApiResponse;
import eureca.capstone.project.admin.response.SuccessMessages;
import eureca.capstone.project.admin.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고/제재 API", description = "사용자 신고 접수 및 관리자 기능 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 건수 조회", description = "오늘 및 전체 신고 건수를 조회합니다.")
    @GetMapping("/reports/counts")
    public ResponseEntity<ApiResponse<ReportCountDto>> getReportCounts() {
        ReportCountDto data = reportService.getReportCounts();
        return ApiResponse.success(SuccessMessages.GET_REPORT_COUNTS_SUCCESS, data);
    }

    @Operation(summary = "신고 내역 목록 조회", description = "신고 내역을 페이징하여 조회합니다. status 파라미터로 필터링할 수 있습니다.")
    @GetMapping("/reports/history")
    public ResponseEntity<ApiResponse<Page<ReportHistoryDto>>> getReportHistoryList(
            @RequestParam(required = false) ReportHistoryStatus status,
            Pageable pageable) {
        Page<ReportHistoryDto> data = reportService.getReportHistoryList(status, pageable);
        return ApiResponse.success(SuccessMessages.GET_REPORT_HISTORY_SUCCESS, data);
    }

    @GetMapping("/reports/restrictions")
    public ResponseEntity<ApiResponse<Page<RestrictionDto>>> getRestrictionList(Pageable pageable) {
        Page<RestrictionDto> data = reportService.getRestrictionList(pageable);
        return ApiResponse.success(SuccessMessages.GET_RESTRICTION_LIST_SUCCESS, data);
    }

    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<Void>> createReport(@RequestBody CreateReportRequestDto request) {
        reportService.createReportAndProcessWithAI(
                request.getUserId(),
                request.getTransactionFeedId(),
                request.getReportTypeId(),
                request.getReason()
        );
        return ApiResponse.success(SuccessMessages.CREATE_REPORT_SUCCESS);
    }

    @PatchMapping("/reports/history/{reportHistoryId}/process")
    public ResponseEntity<ApiResponse<Void>> processReportByAdmin(
            @PathVariable Long reportHistoryId,
            @RequestBody ProcessReportDto request) {
        reportService.processReportByAdmin(reportHistoryId, request);
        return ApiResponse.success(SuccessMessages.PROCESS_REPORT_SUCCESS);
    }

    @GetMapping("/restrict-expired-list")
    public ResponseEntity<ApiResponse<RestrictExpiredResponseDto>> restrictExpiredList() {
        RestrictExpiredResponseDto data = reportService.getRestrictExpiredList();
        return ApiResponse.success(SuccessMessages.GET_EXPIRED_RESTRICTIONS_SUCCESS, data);
    }

    @PostMapping("/restrict-expired")
    public ResponseEntity<ApiResponse<Void>> expireRestrictions(@RequestBody UpdateRestrictionStatusRequestDto request) {
        reportService.expireRestrictions(request.getRestrictionTargetIds());
        return ApiResponse.success(SuccessMessages.UPDATE_RESTRICTION_STATUS_SUCCESS);
    }
}