package eureca.capstone.project.admin.auth.component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("[인가 실패] URI: {}, 원인: {}", request.getRequestURI(), accessDeniedException.getMessage());
        response.setStatus(HttpServletResponse.SC_OK); // 클라이언트와 약속된 응답 형식에 따라 200으로 설정
        response.setContentType("application/json;charset=UTF-8");
        String scForbiddenJson = """
                {
                  "statusCode": 403,
                  "message": "fail",
                  "data": "false"
                }
                """;
        response.getWriter().write(scForbiddenJson);
    }
}
