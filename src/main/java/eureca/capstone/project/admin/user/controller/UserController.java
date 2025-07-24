package eureca.capstone.project.admin.user.controller;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.user.dto.request.UpdateUserRequestDto;
import eureca.capstone.project.admin.user.dto.response.MyReportResponseDto;
import eureca.capstone.project.admin.user.dto.response.UpdateUserResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserPageResponseDto;
import eureca.capstone.project.admin.user.dto.response.UserReportResponseDto;
import eureca.capstone.project.admin.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "사용자 관리 API", description = "사용자 목록 조회 및 상태 변경 API")
@RequestMapping("/admin/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 목록 조회", description = """
    ## 전체 사용자 목록을 페이징하여 조회합니다.
    `keyword` 파라미터를 통해 이메일 또는 닉네임으로 검색할 수 있습니다.

    ***

    ### 📥 요청 파라미터 (Query Parameters)
    | 이름 | 타입 | 필수 | 설명 | 기타 |
    |---|---|:---:|---|---|
    | `keyword` | `String` | X | 검색어 (이메일 또는 닉네임) | |
    | `pageable` | `Object`| X | 페이지 정보 (`page`, `size`, `sort`) | 페이지 정보 비어서 보내도 됩니다.(default로 size=20 적용) |

    ### 🔑 권한
    * 관리자 권한 필요
    """)
    @GetMapping
    public BaseResponseDto<UserPageResponseDto> getUserList(
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable
    ) {
        UserPageResponseDto userPageResponseDto = userService.getUserList(keyword, pageable);
        return BaseResponseDto.success( userPageResponseDto);
    }

    @Operation(summary = "사용자 상태 변경", description = """
    ## 특정 사용자의 상태를 활성화(`ACTIVE`) 또는 차단(`BANNED`)으로 변경합니다.

    ***

    ### 📥 요청 파라미터 (Path Variable)
    | 이름 | 타입 | 필수 | 설명 |
    |---|---|:---:|---|
    | `userId` | `Long` | O | 상태를 변경할 사용자의 ID |

    ### 📥 요청 바디 (Request Body)
    ```json
    {
      "isBanned": true
    }
    ```
    ### 요청 바디 필드 설명
    * `isBanned` : boolean 값으로 true, false를 받습니다. (차단, 활성화)
    ### 🔑 권한
    * 관리자 권한 필요

    ### ❌ 주요 실패 코드
    * `70010` (USER_NOT_FOUND): 해당 `userId`의 사용자가 존재하지 않을 경우
    """)
    @PatchMapping("/{userId}/ban")
    public BaseResponseDto<UpdateUserResponseDto> updateUserStatus(@PathVariable Long userId,
                                                                   @RequestBody UpdateUserRequestDto updateUserRequestDto) {
        UpdateUserResponseDto updateUserResponseDto = userService.updateUserStatus(userId, updateUserRequestDto);
        return BaseResponseDto.success(updateUserResponseDto);
    }

    @Operation(summary = "사용자 신고 당한 내역 조회", description = """
    ## 특정 사용자가 피신고자로서 받은 모든 신고 내역 목록을 조회합니다.

    ***

    ### 📥 요청 파라미터 (Path Variable)
    | 이름 | 타입 | 필수 | 설명 |
    |---|---|:---:|---|
    | `userId` | `Long` | O | 신고 내역을 조회할 사용자의 ID |

    ### 🔑 권한
    * 관리자 권한 필요

    ### ❌ 주요 실패 코드
    * `70010` (USER_NOT_FOUND): 해당 `userId`의 사용자가 존재하지 않을 경우
    """)
    @GetMapping("/{userId}/report-list")
    public BaseResponseDto<List<UserReportResponseDto>> getUserReport(@PathVariable Long userId) {

        List<UserReportResponseDto> userReportResponseDto = userService.getUserReport(userId);
        return BaseResponseDto.success(userReportResponseDto);
    }

}
