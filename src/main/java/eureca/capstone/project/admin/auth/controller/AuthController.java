package eureca.capstone.project.admin.auth.controller;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.auth.dto.request.LoginRequestDto;
import eureca.capstone.project.admin.auth.dto.response.LoginResponseDto;
import eureca.capstone.project.admin.auth.service.TokenService;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.common.service.RedisService;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RedisService redisService;


    @PostMapping("/login")
    public BaseResponseDto<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );
        log.info("authentication: {}", authentication);

        String accessToken = tokenService.generateToken(
                (CustomUserDetailsDto) authentication.getPrincipal(),
                httpServletResponse
        );

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .build();
        BaseResponseDto<LoginResponseDto> success = BaseResponseDto.success(loginResponseDto);
        log.info("success: {}", success);

        return success;
    }

    @PostMapping("/logout")
    public BaseResponseDto<Void> logout(@AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto) {
        // 요청 값 로그 출력
        log.info("customUserDetailsDto: {}", customUserDetailsDto);
        // Refresh Token 삭제
        redisService.deleteValue(RedisConstant.RedisRefreshToken + customUserDetailsDto.getUserId());
        // 반환값 생성 및 출력
        BaseResponseDto<Void> success = BaseResponseDto.voidSuccess();
        log.info("success: {}", success);
        // 응답값 반환
        return success;
    }
}
