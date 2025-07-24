package eureca.capstone.project.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DashboardResponseDto {
    // 회원 관리
    private long todayUserCount;
    private long totalUserCount;

    // 신고 관리
    private long todayReportCount;
    private long totalReportCount;

    // 시세 통계 (그래프용)
    private List<HourlyPriceStatDto> priceStats;

    // 거래량 통계 (그래프용)
    private TransactionVolumeStatDto volumeStats;
}
