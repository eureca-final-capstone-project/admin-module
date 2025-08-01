package eureca.capstone.project.admin.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String accessToken;
    private Long userId;
}
