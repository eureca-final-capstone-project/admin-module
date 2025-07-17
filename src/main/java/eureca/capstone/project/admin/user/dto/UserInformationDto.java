package eureca.capstone.project.admin.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserInformationDto {
    private Long userId;
    private String email;
    private String password;
    private Set<String> roles;
    private Set<String> authorities;


    public static UserInformationDto emptyDto() {
        return UserInformationDto.builder().build();
    }
}
