package eureca.capstone.project.admin.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import eureca.capstone.project.admin.auth.constant.FilterConstant;
import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.auth.util.CookieUtil;
import eureca.capstone.project.admin.auth.util.JwtUtil;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import eureca.capstone.project.admin.common.dto.base.ErrorResponseDto;
import eureca.capstone.project.admin.common.exception.code.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 화이트리스트 통과 처리
        if (isPassListed(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader;
        // 헤더 값 추출 (액세스 토큰에 대한 요청인지, 토큰 재발급에 대한 요청인지 경로값으로 구분)
        if (requestURI.equals(FilterConstant.REFRESH_PATH)) {
            String refreshTokenByCookie = cookieUtil.extractTokenFromCookie(request);
            log.info("[JwtFilter] Refresh token by cookie is: {}", refreshTokenByCookie);
            authHeader = "Bearer " + refreshTokenByCookie;
        } else {
            authHeader = request.getHeader("Authorization");
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header missing or invalid: {}", authHeader);
            writeErrorResponse(response, ErrorCode.MISSING_TOKEN);
            return;
        }

        String token = authHeader.substring(7);
        try {
            // 토큰이 유효한지 검증 (만료시간 + 시그니처 검증, 만약 예외발생시 catch 로 처리)
            jwtUtil.isValidToken(token);

            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            Set<String> roles = jwtUtil.extractRoles(token);
            Set<String> authorities = jwtUtil.extractAuthorities(token);

            // roles + authorities → GrantedAuthority 리스트로 통합
            List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
            for (String role : roles) grantedAuthorities.add(new SimpleGrantedAuthority(role)); // ROLE_ 접두사 이미 포함돼 있음
            for (String authority : authorities)
                grantedAuthorities.add(new SimpleGrantedAuthority(authority)); // 그대로 READ, WRITE 등

            // UserDetails 생성
            CustomUserDetailsDto userDetails =
                    new CustomUserDetailsDto(userId, email, "", grantedAuthorities);
            // 인증 객체 생성 및 SecurityContext 등록
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException e) {
            log.warn("ExpiredJwtException {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.TOKEN_EXPIRED);
            return;
        } catch (SignatureException e) {
            log.warn("SignatureException {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.INVALID_SIGNATURE);
            return;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("MalformedJwtException |  UnsupportedJwtException | IllegalArgumentException {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.MALFORMED_TOKEN);
            return;
        }
        chain.doFilter(request, response);
    }
    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        log.info("ErrorCode : {}", errorCode);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        BaseResponseDto<ErrorResponseDto> fail = BaseResponseDto.fail(errorCode);
        log.info("fail : {}", fail);

        String json = new ObjectMapper().writeValueAsString(fail);
        response.getWriter().write(json);
    }

    private boolean isPassListed(String uri) {
        for (String path : FilterConstant.whiteList) {
            if (path.endsWith("/**")) {
                String basePath = path.replace("/**", "");
                if (uri.startsWith(basePath)) return true;
            } else {
                if (uri.equals(path)) return true;
            }
        }
        return false;
    }
}
