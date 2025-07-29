package eureca.capstone.project.admin.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class RefreshResponseDto {
    private String accessToken;
}