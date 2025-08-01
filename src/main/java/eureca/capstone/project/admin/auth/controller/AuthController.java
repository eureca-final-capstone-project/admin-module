package eureca.capstone.project.admin.auth.controller;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.auth.dto.request.LoginRequestDto;
import eureca.capstone.project.admin.auth.dto.response.LoginResponseDto;
import eureca.capstone.project.admin.auth.service.TokenService;
import eureca.capstone.project.admin.common.constant.RedisConstant;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.common.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 관리", description = "관리자 로그인 및 로그아웃을 처리하는 API") // swagger
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedisService redisService;

    @Operation(summary = "관리자 로그인", description = """
            ### 이메일과 비밀번호로 로그인하여 Access Token을 발급받습니다.
            ***
            * 관리자 계정
                * email: admin@admin.com
                * password: admin
            """)
    @PostMapping("/login")
    public BaseResponseDto<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );
        log.info("authentication: {}", authentication);

        CustomUserDetailsDto customUserDetailsDto = (CustomUserDetailsDto) authentication.getPrincipal();

        String accessToken = tokenService.generateToken(
                (CustomUserDetailsDto) authentication.getPrincipal(),
                httpServletResponse
        );

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .userId(customUserDetailsDto.getUserId())
                .build();
        BaseResponseDto<LoginResponseDto> success = BaseResponseDto.success(loginResponseDto);
        log.info("success: {}", success);

        return success;
    }

    @Operation(summary = "관리자 로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다. 서버에 저장된 Refresh Token이 삭제됩니다. <br> **(주의: 요청 시 Authorization 헤더에 유효한 Access Token을 포함해야 합니다.)**") // swagger
    @PostMapping("/logout")
    public BaseResponseDto<Void> logout(@AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto) {
        // 요청 값 로그 출력
        log.info("customUserDetailsDto: {}", customUserDetailsDto);
        // Refresh Token 삭제
        redisService.deleteValue(RedisConstant.REDIS_REFRESH_TOKEN + customUserDetailsDto.getUserId());
        // 반환값 생성 및 출력
        BaseResponseDto<Void> success = BaseResponseDto.voidSuccess();
        log.info("success: {}", success);
        // 응답값 반환
        return success;
    }
}