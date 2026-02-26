package com.example.shiftmate.domain.shiftTemplate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TemplateShiftStaffReqDto {

    @NotNull(message = "필요 인원은 필수입니다.")
    @Min(value = 1, message = "필요 인원은 1명 이상이어야 합니다.")
    private Integer requiredStaff;

    @Size(max = 40, message = "시프트 이름은 40자 이하여야 합니다.")
    @Pattern(regexp = "^$|.*\\S.*", message = "시프트 이름은 공백만 입력할 수 없습니다.")
    private String name;

}
