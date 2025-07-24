package eureca.capstone.project.admin.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "거래량 응답 DTO")
public class TransactionVolumeStatDto {
    @Schema(description = "판매 유형(일반 판매, 입찰 판매)", example = "일반 판매")
    private String salesType;      // 일반 판매 / 입찰 판매
    @Schema(description = "거래량 통계 타입(HOUR, DAY)", example = "HOUR")
    private String statisticType;  // HOUR/DAY
    @Schema(description = "거래량 통계")
    private List<VolumeStatDto> volumes;
}
