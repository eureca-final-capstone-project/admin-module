package eureca.capstone.project.admin.report.controller;

import eureca.capstone.project.admin.report.dto.response.*;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.report.service.RestrictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "제재 API", description = "사용자 제재 관련 관리자 기능 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class RestrictionController {

    private final RestrictionService restrictionService;


    @Operation(summary = "제재 내역 목록 조회", description = """
            ## 제재 대상 내역을 페이징하여 조회합니다.
            `statusCode`와 `keyword`로 필터링 및 검색이 가능합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 | 기타 |
            |---|---|:---:|---|---|
            | `statusCode` | `String` | X | 필터링할 제재 상태 | |
            | `keyword` | `String` | X | 검색어 (제재자 이메일) | |
            | `pageable` | `Object`| X | 페이지 정보 (`page`, `size`, `sort`) | 페이지 정보 비어서 보내도 됩니다.(default로 size=20 적용) |
            
            ### 🔑 권한
            * 관리자 권한 필요
            
            ### 📝 참고 사항
            * **statusCode 목록**: `PENDING`(검수 대기중), `COMPLETED`(제재 완료), `REJECTED`(제재 미승인)
            """)
    @GetMapping("/restrictions")
    public BaseResponseDto<Page<RestrictionDto>> getRestrictionList(
            @Parameter(description = "필터링할 제재 상태 (PENDING, COMPLETED, REJECTED)") @RequestParam(required = false) String statusCode,
            @Parameter(description = "검색어 (제재자 이메일)") @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return BaseResponseDto.success(restrictionService.getRestrictionListByStatusCode(statusCode, keyword, pageable));
    }

    @Operation(summary = "제재 ID로 신고 내역 조회", description = """
            ## 특정 제재와 연관된 모든 신고 내역을 조회합니다.
            하나의 제재는 여러 건의 신고를 통해 누적될 수 있습니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `restrictId` | `Long` | O | 조회할 제재의 ID |
            
            ### 🔑 권한
            * 관리자 권한 필요
            
            ### 📝 참고 사항
            * 이 API는 페이징을 지원하지 않으며, 해당 제재와 관련된 모든 신고 내역을 반환합니다.
            """)
    @GetMapping("/restricts/{restrictId}/report-list")
    public BaseResponseDto<List<RestrictionReportResponseDto>> getRestrictionReportHistory(@PathVariable("restrictId") Long restrictId) {
        return BaseResponseDto.success(restrictionService.getRestrictionReportHistory(restrictId));
    }

    @Operation(summary = "제재 승인", description = """
            ## 관리자가 제재 대상 건에 대해 최종 승인 처리를 합니다.
            승인 시 제재 유형에 따라 실제 제재(계정 상태 변경, 권한 만료일 설정 등)가 적용됩니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `restrictionTargetId` | `Long` | O | 승인할 제재의 ID |
            
            ### 🔑 권한
            * 관리자 권한 필요
            
            ### ❌ 주요 실패 코드
            * `70012` (ALREADY_PROCESSED_RESTRICTION): 이미 처리된 제재일 경우
            * `70011` (RESTRICTION_TARGET_NOT_FOUND): 해당 `restrictionTargetId`의 제재 대상이 존재하지 않을 경우
            * `70001` (INVALID_ENUM_VALUE): 경로의 `restrictionTargetId`가 숫자가 아닐 경우
            """)
    @PatchMapping("/restrict/{restrictionTargetId}/accept")
    public BaseResponseDto<Void> acceptRestriction(@PathVariable("restrictionTargetId") Long restrictionTargetId) {
        restrictionService.acceptRestrictions(restrictionTargetId);
        return BaseResponseDto.success(null);
    }

    @Operation(summary = "제재 미승인", description = """
            ## 관리자가 제재 대상 건에 대해 최종 거절(미승인) 처리를 합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `restrictionTargetId` | `Long` | O | 미승인할 제재의 ID |
            
            ### 🔑 권한
            * 관리자 권한 필요
            
            ### ❌ 주요 실패 코드
            * `70012` (ALREADY_PROCESSED_RESTRICTION): 이미 처리된 제재일 경우
            * `70011` (RESTRICTION_TARGET_NOT_FOUND): 해당 `id`의 제재 대상이 존재하지 않을 경우
            * `70001` (INVALID_ENUM_VALUE): 경로의 `id`가 숫자가 아닐 경우
            """)
    @PatchMapping("/restrict/{restrictionTargetId}/reject")
    public BaseResponseDto<Void> rejectRestriction(@PathVariable("restrictionTargetId") Long restrictionTargetId) {
        restrictionService.rejectRestrictions(restrictionTargetId);
        return BaseResponseDto.success(null);
    }
}