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

    @Operation(summary = "대시보드 데이터 조회", description = "관리자 페이지 대시보드에 필요한 모든 데이터를 조회합니다. \nsalesType = \"일반 판매\" or \"입찰 판매\". (salesType을 명시하지 않을 경우 default로 \"일반 판매\"로 지정됩니다.)")
    @GetMapping
    public BaseResponseDto<DashboardResponseDto> getDashboardData(
            @RequestParam(value="salesType", defaultValue="일반 판매") String salesType) {
        DashboardResponseDto dashboardData = dashboardService.getDashboardData(salesType);
        return BaseResponseDto.success(dashboardData);
    }

    @Operation(summary="거래량 통계 조회",  description = "거래량 통계를 조회하는 api입니다. \nsalesType = \"일반 판매\" or \"입찰 판매\". (salesType을 명시하지 않을 경우 default로 \"일반 판매\"로 지정됩니다.)")
    @GetMapping("/volume-stats")
    public BaseResponseDto<TransactionVolumeStatDto> getVolumeStats(
            @RequestParam(value="salesType", defaultValue="일반 판매") String salesType) {
        return BaseResponseDto.success(dashboardService.transactionVolumeStatData(salesType));
    }
}
