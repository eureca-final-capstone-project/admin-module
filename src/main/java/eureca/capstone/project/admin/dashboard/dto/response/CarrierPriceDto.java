package eureca.capstone.project.admin.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "시세통계 응답 DTO")
public class CarrierPriceDto {
    @Schema(description = "통신사 명(SKT, KT, LG U+)", example = "SKT")
    private String carrierName; // "SKT", "KT", "LGU+"
    @Schema(description = "100MB 당 평균 시세 (거래내역 없을 경우 NULL 반환)", example = "100")
    private Long pricePerMb; // 1GB당 평균 시세
}
