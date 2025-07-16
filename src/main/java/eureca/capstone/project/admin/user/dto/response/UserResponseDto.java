package eureca.capstone.project.admin.user.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class UserResponseDto {
    Long userId;
    String email;
    String nickName;
    Integer telecomCompanyId;
    String phoneNumber;
    LocalDateTime createdAt;
    Integer status;
    Integer reportCount;
}
