package com.example.shiftmate.domain.employeePreference.dto.request;

import com.example.shiftmate.domain.employeePreference.entity.PreferenceType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PreferenceUpdateReqDto {

    @NotNull(message = "선호 타입은 필수입니다.")
    private PreferenceType preferenceType;
}
