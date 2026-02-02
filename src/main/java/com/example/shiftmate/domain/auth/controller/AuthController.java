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

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ApiResponse.success(response);
    }

}
