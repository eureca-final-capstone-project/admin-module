package eureca.capstone.project.admin.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VolumeStatDto {
    private String date; // 날짜 (예: "2025-07-16")
    private Integer hour;    // 시간 (0 ~ 23)
    private long saleVolume;
}
