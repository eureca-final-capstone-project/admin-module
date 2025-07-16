package eureca.capstone.project.admin.dashboard.controller;

import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "대시보드 API", description = "관리자 대시보드 관련 API")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 데이터 조회", description = "관리자 페이지 대시보드에 필요한 모든 데이터를 조회합니다.")
    @GetMapping
    public BaseResponseDto<DashboardResponseDto> getDashboardData() {
        DashboardResponseDto dashboardData = dashboardService.getDashboardData();
        return BaseResponseDto.success(dashboardData);
    }
}
