package eureca.capstone.project.admin.report.controller;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.report.dto.request.CreateReportRequestDto;
import eureca.capstone.project.admin.report.dto.request.ProcessReportDto;
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

@Tag(name = "신고 API", description = "사용자 신고 접수 및 신고 관련 관리자 기능 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 건수 조회", description = """
            ## 오늘 및 전체 신고 건수를 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 요청 파라미터가 없습니다.
            
            ### 🔑 권한
            * 관리자 권한 필요
            
            ### 📝 참고 사항
            * 대시보드 조회 API에도 오늘 및 전체 신고 건수 포함되어있습니다.
            """)
    @GetMapping("/reports/counts")
    public BaseResponseDto<ReportCountResponseDto> getReportCounts() {
        return BaseResponseDto.success(reportService.getReportCounts());
    }

    @Operation(summary = "신고 내역 목록 조회", description = """
            ## 모든 사용자의 신고 내역을 페이징하여 조회합니다.
            `statusCode`와 `keyword`로 필터링 및 검색이 가능합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Query Parameters)
            | 이름 | 타입 | 필수 | 설명 | 기타 |
            |---|---|:---:|---|---|
            | `statusCode` | `String` | X | 필터링할 신고 상태 | |
            | `keyword` | `String` | X | 검색어 (신고자 이메일) | |
            | `pageable` | `Object`| X | 페이지 정보 (`page`, `size`, `sort`) | 페이지 정보 비어서 보내도 됩니다.(default로 size=20 적용) |
            
            ### 🔑 권한
            * 관리자 권한 필요
            
            ### 📝 참고 사항
            * **statusCode 목록**:
                * `PENDING`: 검수 대기중
                * `AI_ACCEPTED`: AI 승인
                * `AI_REJECTED`: AI 거절
                * `ADMIN_ACCEPTED`: 관리자 승인
                * `ADMIN_REJECTED`: 관리자 거절
                * `COMPLETED`: 제재 완료
                * `REJECTED`: 제재 미승인
            """)
    @GetMapping("/reports/history")
    public BaseResponseDto<Page<ReportHistoryDto>> getReportHistoryList(
            @Parameter(description = "필터링할 신고 상태 <br>(PENDING, AI_ACCEPTED, AI_REJECTED, ADMIN_ACCEPTED, ADMIN_REJECTED, COMPLETED, REJECTED)")
            @RequestParam(required = false) String statusCode,
            @Parameter(description = "검색어 (신고자 이메일)")
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return BaseResponseDto.success(reportService.getReportHistoryListByStatusCode(statusCode, keyword, pageable));
    }

    @Operation(summary = "신고 내역 상세 조회", description = """
            ## 특정 신고 내역을 상세 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `reportId` | `Long` | O | 조회할 신고의 ID |
            
            ### 🔑 권한
            * 관리자 권한 필요
            """)
    @GetMapping("/reports/{reportId}/detail")
    public BaseResponseDto<ReportDetailResponseDto> getReportDetail(@PathVariable("reportId") Long reportId) {
        return BaseResponseDto.success(reportService.getReportDetail(reportId));
    }




    @Operation(summary = "게시글 신고 접수", description = """
            ## 게시글을 신고하면 AI가 1차 검토 후 자동으로 처리 상태를 변경합니다.
            한 사용자의 모든 게시글 중 한번만 신고가 가능합니다. => DUPLICATE_REPORT
            
            ***
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "transactionFeedId": 1,
              "reportTypeId": 1,
              "reason": "욕설 및 비방이 포함된 게시글입니다."
            }
            ```
            
            ### 🔑 권한
            * `ROLE_USER`(사용자 로그인 필요), `ROLE_ADMIN`
            
            ### ❌ 주요 실패 코드
            * `70007` (DUPLICATE_REPORT): 이미 신고한 게시글인 경우
            * `70006` (TRANSACTION_FEED_NOT_FOUND): 신고할 게시글이 존재하지 않는 경우
            * `70004` (REPORT_TYPE_NOT_FOUND): 신고 유형이 존재하지 않는 경우
            * `70008` (AI_REVIEW_FAILED): AI 검토 중 오류가 발생한 경우
            
            ### 📝 참고 사항
            * **reportTypeId 목록**:
                * `1`: 욕설 및 비속어 포함
                * `2`: 주제 불일치
                * `3`: 음란 내용 포함
                * `4`: 외부 채널 유도
                * `5`: 비방/저격 포함
            """)
    @PostMapping("/reports")
    public BaseResponseDto<Void> createReport(@RequestBody CreateReportRequestDto request,
                                              @AuthenticationPrincipal CustomUserDetailsDto userDetailsDto) {
        reportService.createReportAndProcessWithAI(
                userDetailsDto.getUserId(),
                request.getTransactionFeedId(),
                request.getReportTypeId(),
                request.getReason()
        );
        return BaseResponseDto.success(null);
    }

    @Operation(summary = "관리자 신고 처리", description = """
            ## 관리자가 특정 신고를 승인 또는 거절 처리합니다.
            AI가 판별하기 모호하여 `PENDING` 상태이거나, AI가 `AI_REJECTED` 처리한 신고 건에 대해서만 처리가 가능합니다.
            
            ***
            
            ### 📥 요청 파라미터 (Path Variable)
            | 이름 | 타입 | 필수 | 설명 |
            |---|---|:---:|---|
            | `reportHistoryId` | `Long` | O | 처리할 신고 내역 ID |
            
            ### 📥 요청 바디 (Request Body)
            ```json
            {
              "approved": true
            }
            ```
            ### 요청 바디 필드 설명
            * `approved` : boolean 값으로 true, false를 받습니다. (승인, 거절)
            
            ### 🔑 권한
            * 관리자 권한 필요
            
            ### ❌ 주요 실패 코드
            * `70002` (ALREADY_PROCESSED_REPORT): 이미 처리된 신고일 경우
            * `70003` (REPORT_NOT_FOUND): 해당 `reportHistoryId`의 신고 내역이 존재하지 않을 경우
            * `70001` (INVALID_ENUM_VALUE): 경로의 `reportHistoryId`가 숫자가 아닐 경우
            """)
    @PatchMapping("/reports/history/{reportHistoryId}/process")
    public BaseResponseDto<Void> processReportByAdmin(
            @Parameter(description = "처리할 신고 ID") @PathVariable("reportHistoryId") Long reportHistoryId,
            @RequestBody ProcessReportDto request) {
        reportService.processReportByAdmin(reportHistoryId, request);
        return BaseResponseDto.success(null);
    }

    @Operation(summary = "신고 유형 목록 조회", description = """
            ## 사용자가 게시글을 신고할 때 선택할 수 있는 신고 유형 전체 목록을 조회합니다.
            
            ***
            
            ### 📥 요청 파라미터
            * 요청 파라미터가 없습니다.
            
            ### ✅ 성공 응답 (Success Response)
            ```json
            {
              "statusCode": 200,
              "message": "success",
              "data": [
                {
                  "reportTypeId": 1,
                  "typeName": "욕설 및 비속어 포함"
                },
                {
                  "reportTypeId": 2,
                  "typeName": "주제 불일치"
                },
                {
                  "reportTypeId": 3,
                  "typeName": "음란 내용 포함"
                },
                {
                  "reportTypeId": 4,
                  "typeName": "외부 채널 유도"
                },
                {
                  "reportTypeId": 5,
                  "typeName": "비방/저격 포함"
                }
              ]
            }
            ```
            
            ### 🔑 권한
            * 관리자 권한 필요
            """)
    @GetMapping("/report-types")
    public BaseResponseDto<List<ReportTypeResponseDto>> getReportTypes() {
        List<ReportTypeResponseDto> reportTypes = reportService.getReportTypes();
        return BaseResponseDto.success(reportTypes);
    }
}