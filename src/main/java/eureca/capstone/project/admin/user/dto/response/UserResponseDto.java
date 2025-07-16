package eureca.capstone.project.admin.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    Long userId;
    String email;
    String nickName;
    String telecomCompany;
    String phoneNumber;
    LocalDateTime createdAt;
    String status;
    Long reportCount;
}
