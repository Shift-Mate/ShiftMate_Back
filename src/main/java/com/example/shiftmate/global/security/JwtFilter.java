package com.example.shiftmate.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

// 매 요청마다 JWT를 검사하는 필터
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_TYPE_ACCESS = "access";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 토큰이 있으면 유효성 검사 후 인증 처리
        if (StringUtils.hasText(token)) {
            // 유효하지 않으면 예외 발생
            jwtProvider.validateToken(token);

            // access 토큰인지 확인 (refresh면 인증 처리 안 함)
            String category = jwtProvider.getCategory(token);
            if (TOKEN_TYPE_ACCESS.equals(category)) {

                // 토큰에서 이메일 추출 후 사용자 로딩
                String email = jwtProvider.getEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 인증 객체를 SecurityContext에 등록
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 넘김
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 Bearer 토큰만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}