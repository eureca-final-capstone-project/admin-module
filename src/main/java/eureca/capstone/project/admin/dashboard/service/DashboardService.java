package eureca.capstone.project.admin.dashboard.service;

import eureca.capstone.project.admin.dashboard.dto.response.AdminEmailResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.DashboardResponseDto;
import eureca.capstone.project.admin.dashboard.dto.response.TransactionVolumeStatDto;

public interface DashboardService {
    DashboardResponseDto getDashboardData(String salesType);
    TransactionVolumeStatDto transactionVolumeStatData(String salesType);
    AdminEmailResponseDto getAdminEmail(Long userId);
}
