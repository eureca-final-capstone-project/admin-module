package eureca.capstone.project.admin.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "시세통계 응답 DTO")
public class HourlyPriceStatDto {
    @Schema(description = "시세 통계 날짜", example = "2025-07-16")
    private String date; // 날짜 (예: "2025-07-16")
    @Schema(description = "시세 통계 시간(0 ~ 23)", example = "21")
    private int hour;    // 시간 (0 ~ 23)
    @Schema(description = "시세 통계 응답")
    private List<CarrierPriceDto> pricesByCarrier;
}
