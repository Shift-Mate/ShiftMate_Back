package com.example.shiftmate.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SocialLoginRequest {
    @NotBlank(message = "Auth code는 필수입니다.")
    private String code; // 프론트에서 전달받은 authorization code (인가 코드)
}