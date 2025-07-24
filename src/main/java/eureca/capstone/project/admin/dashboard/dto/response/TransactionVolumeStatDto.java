package eureca.capstone.project.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransactionVolumeStatDto {
    private String salesType;      // 일반 판매 / 입찰 판매
    private String statisticType;  // HOUR/DAY
    private List<VolumeStatDto> volumes;
}
