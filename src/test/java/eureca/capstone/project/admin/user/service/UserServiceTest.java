package eureca.capstone.project.admin.user.service;

import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.entity.TelecomCompany;
import eureca.capstone.project.admin.common.exception.custom.UserNotFoundException;
import eureca.capstone.project.admin.common.util.StatusManager;
import eureca.capstone.project.admin.user.dto.request.UpdateUserRequestDto;
import eureca.capstone.project.admin.user.dto.response.UpdateUserResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserPageResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import eureca.capstone.project.admin.user.entity.User;
import eureca.capstone.project.admin.user.repository.UserRepository;
import eureca.capstone.project.admin.user.service.Impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatusManager statusManager;

    @InjectMocks
    private UserServiceImpl userService;

    private Pageable pageable;
    private UserResponseDto user1;
    private UserResponseDto user2;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        user1 = new UserResponseDto(100L, "email1@email.com", "nick1", "LG", "010-1111-1111", LocalDateTime.now(), "ACTIVE", 0L);
        user2 = new UserResponseDto(101L, "email2@email.com", "nick2", "KT", "010-2222-2222", LocalDateTime.now(), "BANNED", 2L);
    }

    @Test
    @DisplayName("사용자 목록 조회_검색어 없음_성공")
    void getUserList_NoKeyword_Success() {
        // given
        String keyword = null;
        Page<UserResponseDto> page = new PageImpl<>(List.of(user1, user2), pageable, 2);
        when(userRepository.getUserList(keyword, pageable)).thenReturn(page);

        // when
        UserPageResponseDto result = userService.getUserList(keyword, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(0, result.getPage());
        assertEquals("nick1", result.getContent().get(0).getNickName());
        verify(userRepository).getUserList(keyword, pageable);
    }

    @Test
    @DisplayName("사용자 목록 조회_검색어 있음_성공")
    void getUserList_WithKeyword_Success() {
        // given
        String keyword = "nick1";
        // "nick1"으로 검색 시 user1만 포함된 페이지가 반환될 것을 가정
        Page<UserResponseDto> page = new PageImpl<>(List.of(user1), pageable, 1);
        when(userRepository.getUserList(keyword, pageable)).thenReturn(page);

        // when
        UserPageResponseDto result = userService.getUserList(keyword, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("nick1", result.getContent().get(0).getNickName());
        verify(userRepository).getUserList(keyword, pageable);
    }

    @Test
    @DisplayName("사용자 상태 변경_차단 성공")
    void updateUserStatus_Ban_Success() {
        // given
        Long userId = 100L;
        UpdateUserRequestDto request = UpdateUserRequestDto.builder().isBanned(true).build();
        User userEntity = User.builder().userId(userId).build();
        Status bannedStatus = Status.builder().code("BANNED").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(statusManager.getStatus("USER", "BANNED")).thenReturn(bannedStatus);

        // when
        UpdateUserResponseDto result = userService.updateUserStatus(userId, request);

        // then
        assertNotNull(result);
        assertTrue(result.getIsBanned());
        assertEquals(userId, result.getUserId());
        verify(userRepository).findById(userId);
        verify(statusManager).getStatus("USER", "BANNED");
    }

    @Test
    @DisplayName("사용자 상태 변경_활성화")
    void updateUserStatus_Activate_Success() {
        // given
        Long userId = 101L;

        UpdateUserRequestDto request = UpdateUserRequestDto.builder().isBanned(false).build();
        User userEntity = User.builder().userId(userId).build();
        Status activeStatus = Status.builder().code("ACTIVE").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(statusManager.getStatus("USER", "ACTIVE")).thenReturn(activeStatus);

        // when
        UpdateUserResponseDto result = userService.updateUserStatus(userId, request);

        // then
        assertNotNull(result);
        assertFalse(result.getIsBanned());
        assertEquals(userId, result.getUserId());
        verify(userRepository).findById(userId);
        verify(statusManager).getStatus("USER", "ACTIVE");
    }

    @Test
    @DisplayName("사용자 상태 변경_사용자 없음")
    void updateUserStatus_UserNotFound_Exception() {
        // given
        Long userId = 99L;
        UpdateUserRequestDto request = UpdateUserRequestDto.builder().isBanned(false).build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // then
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUserStatus(userId, request));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 신고 내역 조회_성공")
    void getUserReport_Success() {
        // given
        Long userId = 100L;
        User user = User.builder()
                .userId(userId)
                .email("test@example.com")
                .build();

        List<UserReportResponseDto> reports = List.of(
                new UserReportResponseDto(1L, "욕설 및 비속어 포함", "내용", LocalDateTime.now(), "관리자 승인"),
                new UserReportResponseDto(2L, "주제 불일치", "내용2", LocalDateTime.now(), "AI 승인")
        );

        when(userRepository.getUserReportList(userId)).thenReturn(reports);
        when(userRepository.existsById(userId)).thenReturn(true);

        // when
        List<UserReportResponseDto> result = userService.getUserReport(userId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("욕설 및 비속어 포함", result.get(0).getReportType());
        verify(userRepository).getUserReportList(userId);
    }
}
