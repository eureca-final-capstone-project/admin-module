package eureca.capstone.project.admin.user.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserRequestDto {
    private Boolean isBanned;
}
