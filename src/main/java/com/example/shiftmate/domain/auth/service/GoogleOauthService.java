package com.example.shiftmate.domain.auth.service;

import com.example.shiftmate.domain.auth.dto.response.GoogleTokenResponse;
import com.example.shiftmate.domain.auth.dto.response.GoogleUserResponse;
import com.example.shiftmate.domain.auth.dto.response.SocialUserInfo;
import com.example.shiftmate.domain.user.entity.AuthProvider;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class GoogleOauthService {

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.google.token-uri}")
    private String tokenUri;

    @Value("${oauth.google.user-info-uri}")
    private String userInfoUri;

    private final WebClient webClient = WebClient.create();

    // authorization code -> access token
    public GoogleTokenResponse getToken(String code) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("redirect_uri", redirectUri);
            formData.add("code", code);

            return webClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_TOKEN_FAILED);
        }
    }

    // access token -> user info
    public GoogleUserResponse getUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(GoogleUserResponse.class)
                    .block();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
    }

    // AuthService가 공통 처리할 수 있도록 SocialUserInfo로 변환
    public SocialUserInfo getSocialUserInfo(String code) {
        GoogleTokenResponse token = getToken(code);
        if (token == null || token.getAccessToken() == null) {
            throw new CustomException(ErrorCode.OAUTH_TOKEN_FAILED);
        }

        GoogleUserResponse user = getUserInfo(token.getAccessToken());
        if (user == null || user.getSub() == null) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }

        return SocialUserInfo.builder()
                .provider(AuthProvider.GOOGLE)
                .providerId(user.getSub())
                .email(user.getEmail())
                .name((user.getName() == null || user.getName().isBlank()) ? "google_user" : user.getName())
                .build();
    }
}