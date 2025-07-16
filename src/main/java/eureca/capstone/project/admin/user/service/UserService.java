package eureca.capstone.project.admin.user.service;

import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponseDto> getUserList(Pageable pageable);
}