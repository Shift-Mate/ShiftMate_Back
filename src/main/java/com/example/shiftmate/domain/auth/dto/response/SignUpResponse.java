package com.example.shiftmate.domain.auth.dto.response;

import com.example.shiftmate.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponse {
    private Long id;
    private String email;
    private String name;

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .build();
    }
}
