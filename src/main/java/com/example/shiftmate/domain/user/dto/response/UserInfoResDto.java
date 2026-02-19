package com.example.shiftmate.domain.user.dto.response;

import com.example.shiftmate.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResDto {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;

    public static UserInfoResDto from(User user){
        return UserInfoResDto.builder()
                   .userId(user.getId())
                   .name(user.getName())
                   .email(user.getEmail())
                   .phoneNumber(user.getPhoneNumber())
                   .build();
    }
}
