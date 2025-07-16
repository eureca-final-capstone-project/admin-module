package eureca.capstone.project.admin.user.service;

import eureca.capstone.project.admin.user.dto.response.UserPageResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserPageResponseDto getUserList(Pageable pageable);
}