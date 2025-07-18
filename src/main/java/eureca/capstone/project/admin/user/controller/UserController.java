package eureca.capstone.project.admin.user.controller;

import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.user.dto.request.UpdateUserRequestDto;
import eureca.capstone.project.admin.user.dto.response.UpdateUserResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserPageResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "사용자 관리 API", description = "사용자 목록 조회 및 상태 변경 API")
@RequestMapping("/admin/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 목록 조회", description = "전체 사용자 목록을 조회합니다. 검색어를 통해 이메일 또는 닉네임으로 검색할 수 있습니다.")
    @GetMapping
    public BaseResponseDto<UserPageResponseDto> getUserList(
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable
    ) {
        UserPageResponseDto userPageResponseDto = userService.getUserList(keyword, pageable);
        return BaseResponseDto.success( userPageResponseDto);
    }

    @Operation(summary = "사용자 상태 변경", description = "특정 사용자의 차단/활성화 상태를 변경합니다.")
    @PatchMapping("/{userId}/ban")
    public BaseResponseDto<UpdateUserResponseDto> updateUserStatus(@PathVariable Long userId,
                                                                   @RequestBody UpdateUserRequestDto updateUserRequestDto) {
        UpdateUserResponseDto updateUserResponseDto = userService.updateUserStatus(userId, updateUserRequestDto);
        return BaseResponseDto.success(updateUserResponseDto);
    }

    @Operation(summary = "사용자 신고 내역 조회", description = "특정 사용자가 신고 당한 내역을 조회합니다.")
    @GetMapping("/{userId}/report-list")
    public BaseResponseDto<List<UserReportResponseDto>> getUserReport(@PathVariable Long userId) {

        List<UserReportResponseDto> userReportResponseDto = userService.getUserReport(userId);
        return BaseResponseDto.success(userReportResponseDto);
    }
}
