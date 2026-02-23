package com.example.shiftmate.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleTokenResponse {

    // 구글 토큰 교환 응답의 access_token 매핑
    @JsonProperty("access_token")
    private String accessToken;
}