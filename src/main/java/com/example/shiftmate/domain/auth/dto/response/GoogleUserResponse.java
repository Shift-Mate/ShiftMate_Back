package com.example.shiftmate.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleUserResponse {

    // OpenID Connect userinfo의 고유 식별자
    private String sub;

    // 요구사항: 이메일 필수
    private String email;

    // 표시 이름 (없을 수 있어 fallback 처리)
    private String name;
}