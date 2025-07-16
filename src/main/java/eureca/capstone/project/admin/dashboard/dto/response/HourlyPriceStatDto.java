package eureca.capstone.project.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class HourlyPriceStatDto {
    private String date; // 날짜 (예: "2025-07-16")
    private int hour;    // 시간 (0 ~ 23)
    private List<CarrierPriceDto> pricesByCarrier;
}
