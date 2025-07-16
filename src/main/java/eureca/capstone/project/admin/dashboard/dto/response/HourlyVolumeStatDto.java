package eureca.capstone.project.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HourlyVolumeStatDto {
    private String date; // 날짜 (예: "2025-07-16")
    private int hour;    // 시간 (0 ~ 23)
    private long generalSaleVolume;
    private long auctionSaleVolume;
}
