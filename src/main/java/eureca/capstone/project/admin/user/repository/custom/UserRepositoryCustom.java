package eureca.capstone.project.admin.user.repository.custom;

import eureca.capstone.project.admin.user.dto.UserInformationDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepositoryCustom {
    Page<UserResponseDto> getUserList(String keyword, Pageable pageable);
    List<UserReportResponseDto> getUserReportList(Long userId);
    UserInformationDto findAdminInformation(String email);
}
