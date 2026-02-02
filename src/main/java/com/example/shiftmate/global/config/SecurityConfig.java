package com.example.shiftmate.global.config;
/*
- JWT 기반 인증을 넣기 위한 기본 보안 뼈대
- /auth/**는 로그인/회원가입용으로 열어둠
 */
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 비밀번호를 BCrypt로 단방향 암호화하기 위한 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security의 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API에서는 보통 CSRF를 끄고 토큰 기반으로 인증함
                .csrf(csrf -> csrf.disable())

                // 세션을 사용하지 않고(JWT 방식) 요청마다 인증 처리
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증/인가 규칙 정의
                .authorizeHttpRequests(auth -> auth
                        // 회원가입/로그인 같은 auth API는 누구나 접근 허용
                        .requestMatchers("/auth/**").permitAll()
                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
