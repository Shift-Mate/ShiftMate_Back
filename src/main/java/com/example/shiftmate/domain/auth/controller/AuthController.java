package com.example.shiftmate.domain.auth.controller;

import com.example.shiftmate.domain.auth.dto.request.*;
import com.example.shiftmate.domain.auth.dto.response.SignUpResponse;
import com.example.shiftmate.domain.auth.service.AuthService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/reissue")
    public ApiResponse<AuthResponse> reissue(@Valid @RequestBody RefreshRequest request) {
        // refresh 토큰으로 access/refresh 재발급
        AuthResponse response = authService.reissue(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@Valid @RequestBody LogoutRequest request) {
        // refresh 토큰을 Redis에서 삭제하여 로그아웃 처리
        authService.logout(request);
        return ApiResponse.success("로그아웃 완료");
    }

    /** 비밀번호 재설정 요청: 이메일 입력 시 토큰 발급 (이메일 발송은 추후 연동) */
    @PostMapping("/password-reset/request")
    public ApiResponse<String> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ApiResponse.success("등록된 이메일이 있다면 재설정 링크를 발송했습니다.");
    }

    /** 비밀번호 재설정 실행: 토큰 + 새 비밀번호로 변경 */
    @PostMapping("/password-reset/confirm")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("비밀번호가 변경되었습니다.");
    }
}
