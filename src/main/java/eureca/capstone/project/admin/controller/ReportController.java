package eureca.capstone.project.admin.controller;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/reports/counts")
    public ResponseEntity<ApiResponse<ReportCountDto>> getReportCounts() {
        ReportCountDto data = reportService.getReportCounts();
        return ApiResponse.success(SuccessMessages.GET_REPORT_COUNTS_SUCCESS, data);
    }

    @GetMapping("/reports/history")
    public ResponseEntity<ApiResponse<Page<ReportHistoryDto>>> getReportHistoryList(
            @RequestParam(required = false) String status,
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