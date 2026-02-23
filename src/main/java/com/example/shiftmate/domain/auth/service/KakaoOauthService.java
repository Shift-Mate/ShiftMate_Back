package com.example.shiftmate.domain.auth.service;

import com.example.shiftmate.domain.auth.dto.response.KakaoTokenResponse;
import com.example.shiftmate.domain.auth.dto.response.KakaoUserResponse;
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
public class KakaoOauthService {

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.kakao.token-uri}")
    private String tokenUri;

    @Value("${oauth.kakao.user-info-uri}")
    private String userInfoUri;

    private final WebClient webClient = WebClient.create();

    // 카카오 authorization code로 access Token 획득
    public KakaoTokenResponse getToken(String code){

        try{

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "authorization_code");
            formData.add("client_id",  clientId);
            formData.add("client_secret", clientSecret);
            formData.add("redirect_uri", redirectUri);
            formData.add("code", code);

            return webClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

        }catch(Exception e){
            throw new CustomException(ErrorCode.OAUTH_TOKEN_FAILED);
        }
    }

    // access Token 으로 사용자 정보 조회
    public KakaoUserResponse getUserInfo(String accessToken) {
        try{
            return webClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserResponse.class)
                    .block();
                    
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
    }

    public SocialUserInfo getSocialUserInfo(String code) {
        KakaoTokenResponse token = getToken(code);
        if (token == null || token.getAccessToken() == null) {
            throw new CustomException(ErrorCode.OAUTH_TOKEN_FAILED);
        }

        KakaoUserResponse user = getUserInfo(token.getAccessToken());
        if (user == null || user.getId() == null || user.getKakaoAccount() == null) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }

        return SocialUserInfo.builder()
                .provider(AuthProvider.KAKAO)
                .providerId(String.valueOf(user.getId()))
                .email(user.getKakaoAccount().getEmail()) // 이메일 필수 검증은 AuthService에서 공통 처리
                .name(user.getKakaoAccount().getProfile() != null
                        ? user.getKakaoAccount().getProfile().getNickname()
                        : "kakao_user")
                .build();
    }
    
}
