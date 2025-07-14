package eureca.capstone.project.admin.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AIReviewResponseDto {
    private String result; // AI 판단 결과 (예: "ACCEPT", "REJECT", "PENDING")
    private double confidence; // 판단 신뢰도 (0.0 ~ 1.0)
}
