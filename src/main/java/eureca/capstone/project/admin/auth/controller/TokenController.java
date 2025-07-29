package eureca.capstone.project.admin.auth.controller;


import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.auth.dto.response.RefreshResponseDto;
import eureca.capstone.project.admin.auth.service.TokenService;
import eureca.capstone.project.admin.common.dto.base.BaseResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name="리프레시토큰 재발급 API", description = "리프레시토큰 재발급 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/auth")
public class TokenController {
    private final TokenService tokenService;

    @Operation(
            summary = "Token 재발급 API", description = """
            ### 📌 설명  
            서버에 저장된 **Refresh Token**(쿠키 기반)을 이용하여 새로운 **Token**을 재발급합니다.  
            클라이언트는 기존 Access Token이 만료되었을 때(10001 에러) 이 API를 호출하여 갱신할 수 있습니다.
            
            ---
            
            ### 📥 요청 쿠키
            | 이름           | 타입     | 필수 | 설명                          |
            |----------------|----------|:----:|-------------------------------|
            | `refreshToken` | `String` | O    | 서버에 저장된 리프레시 토큰 |
            
            ---
            
            ### 📤 응답
            | 필드          | 타입     | 설명                         |
            |---------------|----------|------------------------------|
            | `accessToken` | `String` | 재발급된 새로운 액세스 토큰 |
            
            ---
            
            ### 🔑 권한
            * 관리자 (쿠키에 저장된 refreshToken 기반)
            
            ---
            
            ### ❌ 주요 실패 코드
            * **HTTP 401 Unauthorized**: 유효하지 않은 refreshToken 값을 보냈을 경우
            
            ---
            
            ### 📝 예시
            **Request**
            ```
            POST /re-generate-token
            Cookie: refreshToken={your_refresh_token}
            ```
            
            **Response**
            ```json
            {
              "statusCode": 200,
              "message": "success",
              "data": {
                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
              }
            }
            ```
            """
    )
    @PostMapping("/re-generate-token")
    public BaseResponseDto<RefreshResponseDto> reGenerateToken(@AuthenticationPrincipal CustomUserDetailsDto customUserDetailsDto,
                                                               HttpServletResponse httpServletResponse) throws IOException{
        log.info("reGenerateToken customUserDetailsDto: {}", customUserDetailsDto);
        String accessToken = tokenService.reGenerateToken(customUserDetailsDto, httpServletResponse);
        RefreshResponseDto reGenerateTokenResponseDto = RefreshResponseDto.builder()
                .accessToken(accessToken)
                .build();
        log.info("reGenerateTokenResponseDto: {}", reGenerateTokenResponseDto);
        BaseResponseDto<RefreshResponseDto> success = BaseResponseDto.success(reGenerateTokenResponseDto);
        log.info("success: {}", success);
        return success;
    }
}
