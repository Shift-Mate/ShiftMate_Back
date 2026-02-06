package com.example.shiftmate.global.config;

import com.example.shiftmate.global.security.JwtAuthenticationEntryPoint;
import com.example.shiftmate.global.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT 인증 필터 (토큰 검증 + SecurityContext 설정)
    private final JwtFilter jwtFilter;
    // 인증 실패 시 JSON 응답을 내려주는 EntryPoint
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // CORS 허용 출처 목록 (application.properties에서 주입)
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // 비밀번호 단방향 해시(BCrypt) Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 보안 필터 체인 설정 (JWT 기반, 세션 미사용)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // REST API이므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // JWT 사용으로 서버 세션 사용하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청별 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인/회원가입 등 인증 관련 API는 허용
                        .requestMatchers("/auth/**").permitAll()
                        // 나머지 API는 기본적으로 허용 (필요시 authenticated로 변경)
                        .anyRequest().permitAll()
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 전에 등록
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // 인증 실패 시 커스텀 EntryPoint 사용
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 어떤 프론트 주소에서 요청을 허용할지 지정
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더 (Authorization 등)
        configuration.setAllowedHeaders(List.of("*"));

        // 쿠키/인증정보 포함 허용 여부
        configuration.setAllowCredentials(true);

        // 모드 경로에 위 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }



}