package eureca.capstone.project.admin.report.controller;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.report.dto.request.CreateReportRequestDto;
import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
import eureca.capstone.project.admin.report.dto.request.UpdateRestrictionStatusRequestDto;
import eureca.capstone.project.admin.report.dto.response.*;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.report.service.ReportService;
import eureca.capstone.project.admin.report.service.RestrictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "제재 API", description = "사용자 제재 관련 관리자 기능 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RestrictionController {

    private final RestrictionService restrictionService;


    @Operation(summary = "제재 내역 목록 조회", description = "제재 대상 내역을 페이징하여 조회합니다. status 파라미터로 필터링할 수 있습니다.")
    @GetMapping("/restrics")
    public BaseResponseDto<Page<RestrictionDto>> getRestrictionList(
            @Parameter(description = "필터링할 제재 상태 (PENDING, COMPLETED, REJECTED)") @RequestParam(required = false) String statusCode,
            @Parameter(description = "검색어 (신고자 이메일)") @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return BaseResponseDto.success(restrictionService.getRestrictionListByStatusCode(statusCode,keyword, pageable));
    }

    @Operation(summary = "제재 ID로 신고 내역 조회", description = "제재 ID를 통해 연관된 신고 내역을 조회합니다.")
    @GetMapping("restricts/{restrictId}/report-list")
    public BaseResponseDto<List<RestrictionReportResponseDto>> getRestrictionReportHistory(@PathVariable("restrictId") Long restrictId) {
        return BaseResponseDto.success(restrictionService.getRestrictionReportHistory(restrictId));
    }

    @Operation(summary = "제재 승인", description = "관리자 제재 승인 api 입니다.")
    @PatchMapping("/restrict/{id}/accept")
    public BaseResponseDto<Void> acceptRestriction(@PathVariable("id") Long restrictionTargetId) {
        restrictionService.acceptRestrictions(restrictionTargetId);
        return BaseResponseDto.success(null);
    }

    @Operation(summary = "제재 미승인", description = "관리자 제재 미승인(거절) api 입니다.")
    @PatchMapping("/restrict/{id}/reject")
    public BaseResponseDto<Void> rejectRestriction(@PathVariable("id") Long restrictionTargetId) {
        restrictionService.rejectRestrictions(restrictionTargetId);
        return BaseResponseDto.success(null);
    }
}