package eureca.capstone.project.admin.user.repository.custom;

import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<UserResponseDto> getUserList(Pageable pageable);
}
