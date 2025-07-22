package eureca.capstone.project.admin.mypage;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.user.dto.response.MyReportResponseDto;
import eureca.capstone.project.admin.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "마이페이지 API")
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final UserService userService;

    @Operation(summary = "내가 신고한 내역 조회", description = "로그인한 사용자가 다른 사용자를 신고한 내역 목록을 조회합니다.")
    @GetMapping("/reports")
    public BaseResponseDto<List<MyReportResponseDto>> getMyReports(@AuthenticationPrincipal CustomUserDetailsDto userDetailsDto) {
        Long currentUserId = userDetailsDto.getUserId();
        List<MyReportResponseDto> myReportList = userService.getMyReportList(currentUserId);
        return BaseResponseDto.success(myReportList);
    }
}
