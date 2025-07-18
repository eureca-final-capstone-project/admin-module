package eureca.capstone.project.admin.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateUserRequestDto {
    @Schema(description = "차단/활성화 요청. isBanned=true: 차단요청, isBanned=false: 활성화 요청")
    private Boolean isBanned;
}
