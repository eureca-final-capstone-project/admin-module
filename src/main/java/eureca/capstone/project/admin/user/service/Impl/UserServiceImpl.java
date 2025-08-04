package eureca.capstone.project.admin.user.service.Impl;

import eureca.capstone.project.admin.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.admin.common.service.RedisService;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.user.dto.request.UpdateUserRequestDto;
import eureca.capstone.project.admin.user.dto.response.*;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.user.repository.UserRepository;
import eureca.capstone.project.admin.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StatusManager statusManager;
    private final RedisService redisService;

    @Override
    public UserPageResponseDto getUserList(String keyword, Pageable pageable) {

        Page<UserResponseDto> response = userRepository.getUserList(keyword, pageable);

        log.info("[getUserList] 사용자 목록 조회 (keyword: {}): 총 {} 건", keyword, response.getTotalElements());

        return new UserPageResponseDto(response);
    }

    @Transactional
    @Override
    public UpdateUserResponseDto updateUserStatus(Long userId, UpdateUserRequestDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        log.info("[updateUserStatus] 사용자 조회 id: {}", user.getUserId());


        if(request.getIsBanned()){ // 차단 요청
            user.updateUserStatus(statusManager.getStatus("USER", "BANNED"));
            log.info("[updateUserStatus] 사용자 차단: {}", user.getStatus().getCode());

            try {
                String key = "BlackListUser:" + user.getUserId();
                redisService.setValue(key, "restricted", 1, TimeUnit.HOURS);
                log.info("[updateUserStatus] 사용자 ID {}를 Redis 블랙리스트에 추가했습니다. (TTL: 1시간)", user.getUserId());
            } catch (Exception e) {
                log.error("[updateUserStatus] Redis에 블랙리스트 사용자 저장 중 오류 발생: {}", e.getMessage());
            }
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

        if(!userRepository.existsById(userId))
            throw new UserNotFoundException();

        List<UserReportResponseDto> response = userRepository.getUserReportList(userId);

        log.info("[getUserReport] 사용자 신고내역 조회: 총 {} 건", response.size());

        return response;
    }

    @Override
    public Page<MyReportResponseDto> getMyReportList(Long userId, Pageable pageable) {
        Page<MyReportResponseDto> response = userRepository.findMyReportList(userId, pageable);
        log.info("[getMyReportList] 사용자가 신고한 신고내역 조회: 총 {} 건", response.getTotalElements());
        return response;
    }
}
