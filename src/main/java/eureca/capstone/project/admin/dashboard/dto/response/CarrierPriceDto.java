package eureca.capstone.project.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarrierPriceDto {
    private String carrierName; // "SKT", "KT", "LGU+"
    private Long pricePerGb; // 1GB당 평균 시세
}
