package com.example.shiftmate.global.security;

import com.example.shiftmate.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

// 인증 실패(401) 시 REST API용 JSON 응답을 반환하는 EntryPoint
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 인증 실패 상황에서 사용할 기본 에러 코드 지정
        ErrorCode errorCode = ErrorCode.EMPTY_TOKEN;

        // HTTP 상태/응답 타입 설정
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // ApiResponse와 동일한 구조로 에러 객체 구성
        Map<String, Object> errorDetails = Map.of(
            "code", errorCode.name(),
            "message", errorCode.getMessage(),
            "details", java.util.List.of()
        );
        Map<String, Object> responseBody = Map.of(
            "success", false,
            "data", null,
            "error", errorDetails
        );

        // JSON 응답 본문 작성
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
