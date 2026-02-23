package com.example.shiftmate.domain.auth.dto.response;

import com.example.shiftmate.domain.user.entity.AuthProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialUserInfo {
    private AuthProvider provider; // 어떤 소셜 제공자인지
    private String providerId;     // 제공자 쪽 고유 사용자 ID
    private String email;          // 제공자에서 내려준 이메일(없을 수 있음)
    private String name;           // 표시 이름(없을 수 있음)
}