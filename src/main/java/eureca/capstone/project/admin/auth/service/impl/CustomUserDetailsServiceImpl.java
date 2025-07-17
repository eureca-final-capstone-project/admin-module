package eureca.capstone.project.admin.auth.service.impl;

import eureca.capstone.project.admin.auth.dto.common.CustomUserDetailsDto;
import eureca.capstone.project.admin.user.dto.UserInformationDto;
import eureca.capstone.project.admin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("loadUserByUsername : {}", email);
        // 요청 파라미터 로그 출력 및 사용자 정보 추출
        UserInformationDto userInformationDto = userRepository.findAdminInformation(email);

        // 쿼리 결과가 없을 경우 (해당 이메일의 관리자가 없을 경우)
        if (userInformationDto.getUserId() == null) {
            log.warn("존재하지 않거나 관리자 권한이 없는 사용자: {}", email);
            throw new UsernameNotFoundException("사용자를 찾을 수 없거나 관리자 권한이 없습니다: " + email);
        }

        // role, authority 를 GrantedAuthority 변환 및 로그 출력
        Set<String> roles = userInformationDto.getRoles();
        Set<String> authorities = userInformationDto.getAuthorities();
        log.info("role : {}", roles);
        log.info("authorities : {}", authorities);

        // 권한과 역할을 담을 변수 생성
        Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

        // 역할과 권한 추출 및 grantedAuthorities 에 담기
        for (String role : roles) grantedAuthorities.add(new SimpleGrantedAuthority(role));
        for (String authority : authorities) grantedAuthorities.add(new SimpleGrantedAuthority(authority));

        // customUserDetailsDto 반환 객체 생성 및 로그 출력
        CustomUserDetailsDto customUserDetailsDto = CustomUserDetailsDto.builder()
                .userId(userInformationDto.getUserId())
                .email(userInformationDto.getEmail())
                .password(userInformationDto.getPassword())
                .authorities(grantedAuthorities)
                .build();
        log.info("customUserDetailsDto : {}", customUserDetailsDto.toString());

        // return
        return customUserDetailsDto;
    }
}
