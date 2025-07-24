package eureca.capstone.project.admin.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "대시보드 응답 DTO")
public class DashboardResponseDto {
    // 회원 관리
    @Schema(description = "오늘 가입한 회원 수", example = "10")
    private long todayUserCount;
    @Schema(description = "총 가입한 회원 수", example = "100")
    private long totalUserCount;

    // 신고 관리
    @Schema(description = "오늘 신고 수", example = "10")
    private long todayReportCount;
    @Schema(description = "총 신고 수", example = "100")
    private long totalReportCount;

    // 시세 통계 (그래프용)
    @Schema(description = "시세통계")
    private List<HourlyPriceStatDto> priceStats;

    // 거래량 통계 (그래프용)
    @Schema(description = "거래량 통계")
    private TransactionVolumeStatDto volumeStats;
}
