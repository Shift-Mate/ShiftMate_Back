package com.example.shiftmate.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 비밀번호 변경 요청 DTO
// - 현재 비밀번호 검증
// - 새 비밀번호 입력
// - 새 비밀번호 확인값 비교
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangePasswordReqDto {

    // 현재 비밀번호 (필수)
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    // 새 비밀번호 (필수)
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;

    // 새 비밀번호 확인 (필수)
    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;
}