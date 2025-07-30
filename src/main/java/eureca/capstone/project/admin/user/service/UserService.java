package eureca.capstone.project.admin.user.service;

import eureca.capstone.project.admin.user.dto.request.UpdateUserRequestDto;
import eureca.capstone.project.admin.user.dto.response.MyReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UpdateUserResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserPageResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserPageResponseDto getUserList(String keyword, Pageable pageable);
    UpdateUserResponseDto updateUserStatus(Long userId, UpdateUserRequestDto request);
    List<UserReportResponseDto> getUserReport(Long userId);
    Page<MyReportResponseDto> getMyReportList(Long userId, Pageable pageable);
}