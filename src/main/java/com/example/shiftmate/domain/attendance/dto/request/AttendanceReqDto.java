package com.example.shiftmate.domain.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AttendanceReqDto {
    // 프론트에서 스케줄을 클릭하고 핀번호를 입력하는 플로우
    @NotNull(message = "배정된 스케줄 ID는 필수입니다.")
    private Long assignmentId;

    @NotBlank(message = "핀번호는 필수입니다.")
    private String pinCode;
}
