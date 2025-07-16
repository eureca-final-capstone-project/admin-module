package eureca.capstone.project.admin.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class UpdateUserResponseDto {
    private Long userId;
    private Boolean isBanned;
}
