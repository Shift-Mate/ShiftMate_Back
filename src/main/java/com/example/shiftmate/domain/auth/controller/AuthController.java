package com.example.shiftmate.domain.auth.controller;

import com.example.shiftmate.domain.auth.dto.request.SignUpRequest;
import com.example.shiftmate.domain.auth.dto.response.SignUpResponse;
import com.example.shiftmate.domain.auth.service.AuthService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.shiftmate.domain.auth.dto.request.LoginRequest;
import com.example.shiftmate.domain.auth.dto.response.AuthResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    // 인증/회원가입 관련 비즈니스 로직 서비스
    private final AuthService authService;

    // 회원가입 API
    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        // 회원가입 처리 후 사용자 정보 반환
        SignUpResponse response = authService.signUp(request);
        return ApiResponse.success(response);
    }

    // 로그인 API
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 로그인 처리 후 access/refresh 토큰 반환
        AuthResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
}
