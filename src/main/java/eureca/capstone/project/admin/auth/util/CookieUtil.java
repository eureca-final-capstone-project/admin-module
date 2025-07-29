package eureca.capstone.project.admin.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import static eureca.capstone.project.admin.auth.constant.TokenConstant.REFRESH_TOKEN_COOKIE_NAME;
import static eureca.capstone.project.admin.auth.constant.TokenConstant.REFRESH_TOKEN_MAX_AGE_SEC;

@Slf4j
@Component
public class CookieUtil {
    public void createRefreshTokenCookie(String refreshToken, HttpServletResponse httpServletResponse) {
        log.info("[createRefreshTokenCookie] 쿠키 생성 시작 - refreshToken : {}", refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE_SEC)
                .sameSite("None")
                .domain("visiblego.com")
                .build();

        httpServletResponse.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("[createRefreshTokenCookie] 쿠키 설정 완료 - name: {}, maxAge: {}, secure: {}, sameSite: {}",
                REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_MAX_AGE_SEC, true, "None");
    }

    public String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
