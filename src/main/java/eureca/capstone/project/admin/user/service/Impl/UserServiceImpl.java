package eureca.capstone.project.admin.user.service.Impl;

import eureca.capstone.project.admin.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.user.dto.request.UpdateUserRequestDto;
import eureca.capstone.project.admin.user.dto.response.UpdateUserResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserPageResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.user.repository.UserRepository;
import eureca.capstone.project.admin.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StatusManager statusManager;

    @Override
    public UserPageResponseDto getUserList(Pageable pageable) {

        Page<UserResponseDto> response = userRepository.getUserList(pageable);

        log.info("[getUserList] 사용자 목록 조회: 총 {} 건", response.getTotalElements());

        return new UserPageResponseDto(response);
    }

    @Transactional
    @Override
    public UpdateUserResponseDto updateUserStatus(Long userId, UpdateUserRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        log.info("[updateUserStatus] 사용자 조회 id: {}", user.getUserId());


        if(request.getIsBanned()){ // 차단 요청
            user.updateUserStatus(statusManager.getStatus("USER", "BANNED"));
            log.info("[updateUserStatus] 사용자 차단: {}", user.getStatus().getCode());
        }
        else { // 활성화 요청
            user.updateUserStatus(statusManager.getStatus("USER", "ACTIVE"));
            log.info("[updateUserStatus] 사용자 활성화: {}", user.getStatus().getCode());
        }

        return UpdateUserResponseDto.builder()
                .userId(userId)
                .isBanned(request.getIsBanned())
                .build();
    }

    @Override
    public List<UserReportResponseDto> getUserReport(Long userId) {

        List<UserReportResponseDto> response = userRepository.getUserReportList(userId);

        log.info("[getUserReport] 사용자 신고내역 조회: 총 {} 건", response.size());

        return response;
    }


}
