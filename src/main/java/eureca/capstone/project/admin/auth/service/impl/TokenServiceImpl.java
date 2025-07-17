package eureca.capstone.project.admin.auth.service.impl;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.auth.service.TokenService;
import eureca.capstone.project.admin.auth.util.CookieUtil;
import eureca.capstone.project.admin.auth.util.JwtUtil;
import eureca.capstone.project.admin.common.constant.RedisConstant;
import eureca.capstone.project.admin.common.service.RedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisService redisService;

    @Override
    public String generateToken(CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse) {
        // 요청 값 로그 출력
        log.info("Generating token - customUserDetailsDto : {}", customUserDetailsDto);
        log.info("Generating token - httpServletResponse : {}", httpServletResponse);

        // 값 추출
        String email = customUserDetailsDto.getEmail();
        Long userId = customUserDetailsDto.getUserId();
        Set<String> roles = customUserDetailsDto.getRoleStrings();
        Set<String> authorities = customUserDetailsDto.getAuthorityStrings();

        // JWT 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(email, roles, authorities, userId);
        String refreshToken = jwtUtil.generateRefreshToken(email, roles, authorities, userId);
        log.info("Generated access token - accessToken: {}", accessToken);
        log.info("Generated access token - refreshToken: {}", refreshToken);

        // Refresh 토큰 Response 헤더에 할당 및 레디스에 저장 (14일 보관)
        cookieUtil.createRefreshTokenCookie(refreshToken, httpServletResponse);
        redisService.setValue(RedisConstant.REDIS_REFRESH_TOKEN + userId, refreshToken, Duration.ofDays(14));

        // return
        return accessToken;
    }
}
