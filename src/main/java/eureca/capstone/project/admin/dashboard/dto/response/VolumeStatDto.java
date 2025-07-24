package eureca.capstone.project.admin.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VolumeStatDto {
    @Schema(description = "거래 통계 날짜", example = "2025-07-16")
    private String date; // 날짜 (예: "2025-07-16")
    @Schema(description = "거래 통계 시간(0 ~ 23)", example = "21")
    private Integer hour;    // 시간 (0 ~ 23)
    @Schema(description = "거래량", example = "20")
    private long saleVolume;
}
