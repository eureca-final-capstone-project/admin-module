package eureca.capstone.project.admin.auth.service;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
    String generateToken(CustomUserDetailsDto customUserDetailsDto, HttpServletResponse httpServletResponse);
}
