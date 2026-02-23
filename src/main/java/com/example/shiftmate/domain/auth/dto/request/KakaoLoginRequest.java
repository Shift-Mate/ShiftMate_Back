package com.example.shiftmate.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KakaoLoginRequest {

    @NotBlank(message = "Auth code는 필수입니다.")
    private String code;

}
