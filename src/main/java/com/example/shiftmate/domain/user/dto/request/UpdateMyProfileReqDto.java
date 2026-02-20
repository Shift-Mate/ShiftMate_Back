package com.example.shiftmate.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 내 프로필 수정 요청 DTO
// - 이름(name)
// - 전화번호(phoneNumber)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMyProfileReqDto {

    // 사용자 이름 (공백 불가)
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    // 전화번호 (숫자만 10~11자리 허용)
    // 예: 01012345678 / 0212345678
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
            regexp = "^[0-9]{10,11}$",
            message = "전화번호는 숫자 10~11자리여야 합니다."
    )
    private String phoneNumber;
}