package eureca.capstone.project.admin.dashboard.controller;

import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.TransactionVolumeStatDto;
import eureca.capstone.project.admin.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대시보드 API", description = "관리자 대시보드 관련 API")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "대시보드 데이터 조회",
            description = """
            ## 관리자 페이지 대시보드에 필요한 모든 데이터 조회
            * salesType = "일반 판매" or "입찰 판매"
            * salesType을 명시하지 않을 경우 default로 "일반 판매" 지정됨

            ***

            ### 📥 요청 파라미터 (Query Parameters)
            | 이름        | 타입     | 필수 | 설명                                                         |
            |------------|---------|:---:|-------------------------------------------------------------|
            | `salesType` | `String` |  X  | 거래량 조회 시 판매 유형 (일반 판매 또는 입찰 판매)             |

            ### 🔑 권한
            * 관리자 권한 필요

            ### ❌ 주요 실패 코드
            * 70013 (SALES_TYPE_NOT_FOUND): 판매 유형이 존재하지 않는 경우
            * 70014 (STATISTIC_NOT_FOUND): 통계 데이터가 존재하지 않는 경우
            
            ### 📝 참고 사항
            * **salesType** 에 따라  
              - "일반 판매": 거래량 **시간별** 통계 (최근 24시간)  
              - "입찰 판매": 거래량 **일별** 통계 (최근 7일)
            """
    )
    @GetMapping
    public BaseResponseDto<DashboardResponseDto> getDashboardData(
            @RequestParam(value="salesType", defaultValue="일반 판매") String salesType) {
        DashboardResponseDto dashboardData = dashboardService.getDashboardData(salesType);
        return BaseResponseDto.success(dashboardData);
    }

    @Operation(
            summary = "거래량 통계 조회",
            description = """
            ## 거래량 통계만 별도 조회
            * salesType = "일반 판매" or "입찰 판매"
            * salesType을 명시하지 않을 경우 default로 "일반 판매" 지정됨

            ***

            ### 📥 요청 파라미터 (Query Parameters)
            | 이름        | 타입     | 필수 | 설명                                                                      |
            |------------|---------|:---:|--------------------------------------------------------------------------|
            | `salesType` | `String` |  X  | 거래량 조회 시 판매 유형 (일반 판매 또는 입찰 판매)     |

            ### 🔑 권한
            * 관리자 권한 필요

            ### ❌ 주요 실패 코드
            * `70013` (SALES_TYPE_NOT_FOUND): 판매 유형이 존재하지 않는 경우
            * `70014` (STATISTIC_NOT_FOUND): 통계 데이터가 존재하지 않는 경우
            
            ### 📝 참고 사항
            * **결과**: `TransactionVolumeStatDto` 형태로 반환  
            * 시간별(`HOUR`) vs 일별(`DAY`) 통계는 `statisticType` 필드로 구분
            """
    )
    @GetMapping("/volume-stats")
    public BaseResponseDto<TransactionVolumeStatDto> getVolumeStats(
            @RequestParam(value="salesType", defaultValue="일반 판매") String salesType) {
        return BaseResponseDto.success(dashboardService.transactionVolumeStatData(salesType));
    }
}
