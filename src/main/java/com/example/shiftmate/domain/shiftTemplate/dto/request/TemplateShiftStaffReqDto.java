package com.example.shiftmate.domain.shiftTemplate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TemplateShiftStaffReqDto {

    @NotNull(message = "필요 인원은 필수입니다.")
    @Min(value = 1, message = "필요 인원은 1명 이상이어야 합니다.")
    private Integer requiredStaff;

}
