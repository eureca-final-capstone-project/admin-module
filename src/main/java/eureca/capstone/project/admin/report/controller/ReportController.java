package eureca.capstone.project.admin.report.controller;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.report.dto.request.CreateReportRequestDto;
import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.report.dto.request.UpdateRestrictionStatusRequestDto;
import eureca.capstone.project.admin.report.dto.response.*;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "신고/제재 API", description = "사용자 신고 접수 및 관리자 기능 API")
@RestController
@RequestMapping("/admin")
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
            @Parameter(description = "필터링할 신고 상태 <br>(PENDING, AI_ACCEPTED, AI_REJECTED, ADMIN_ACCEPTED, ADMIN_REJECTED, COMPLETED, REJECTED)")
            @RequestParam(required = false) String statusCode,
            @Parameter(description = "검색어 (신고자 이메일)")
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return BaseResponseDto.success(reportService.getReportHistoryListByStatusCode(statusCode, keyword, pageable));
    }

    @Operation(summary = "신고 내역 상세 조회", description = "신고 내역을 상세 조회합니다.")
    @GetMapping("/reports/{reportId}/detail")
    public BaseResponseDto<ReportDetailResponseDto> getReportDetail(@PathVariable("reportId") Long reportId) {
        return BaseResponseDto.success(reportService.getReportDetail(reportId));
    }

    @Operation(summary = "제재 내역 목록 조회", description = "제재 대상 내역을 페이징하여 조회합니다. status 파라미터로 필터링할 수 있습니다.")
    @GetMapping("/restrictions")
    public BaseResponseDto<Page<RestrictionDto>> getRestrictionList(
            @Parameter(description = "필터링할 제재 상태 (PENDING, COMPLETED, REJECTED)") @RequestParam(required = false) String statusCode,
            @Parameter(description = "검색어 (신고자 이메일)") @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return BaseResponseDto.success(reportService.getRestrictionListByStatusCode(statusCode,keyword, pageable));
    }

    @Operation(summary = "제재 ID로 신고 내역 조회", description = "제재 ID를 통해 연관된 신고 내역을 조회합니다.")
    @GetMapping("restricts/{restrictId}/report-list")
    public BaseResponseDto<List<RestrictionReportResponseDto>> getRestrictionReportHistory(@PathVariable("restrictId") Long restrictId) {
        return BaseResponseDto.success(reportService.getRestrictionReportHistory(restrictId));
    }

    @Operation(summary = "게시글 신고 접수", description = "사용자가 게시글을 신고하면 AI가 1차 검토 후 접수합니다.")
    @PostMapping("/reports")
    public BaseResponseDto<Void> createReport(@RequestBody CreateReportRequestDto request,
                                              @AuthenticationPrincipal CustomUserDetailsDto userDetailsDto) {
        reportService.createReportAndProcessWithAI(
                userDetailsDto.getUserId(),
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

    @Operation(summary = "제재 승인", description = "관리자 제재 승인 api 입니다.")
    @PatchMapping("/restrict/{id}/accept")
    public BaseResponseDto<Void> acceptRestriction(@PathVariable("id") Long restrictionTargetId) {
        reportService.acceptRestrictions(restrictionTargetId);
        return BaseResponseDto.success(null);
    }

    @Operation(summary = "제재 미승인", description = "관리자 제재 미승인(거절) api 입니다.")
    @PatchMapping("/restrict/{id}/reject")
    public BaseResponseDto<Void> rejectRestriction(@PathVariable("id") Long restrictionTargetId) {
        reportService.rejectRestrictions(restrictionTargetId);
        return BaseResponseDto.success(null);
    }
}