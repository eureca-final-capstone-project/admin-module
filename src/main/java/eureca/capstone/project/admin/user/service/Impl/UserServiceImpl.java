package eureca.capstone.project.admin.user.service.Impl;

import eureca.capstone.project.admin.user.dto.response.UserPageResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.repository.UserRepository;
import eureca.capstone.project.admin.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserPageResponseDto getUserList(Pageable pageable) {

        Page<UserResponseDto> response = userRepository.getUserList(pageable);

        log.info("[getUserList] 사용자 목록 조회: 총 {} 건", response.getTotalElements());

        return new UserPageResponseDto(response);
    }
}
