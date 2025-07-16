package eureca.capstone.project.admin.user.service.Impl;

import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.repository.UserRepository;
import eureca.capstone.project.admin.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Override
    public Page<UserResponseDto> getUserList(Pageable pageable) {

        return null;
    }
}
