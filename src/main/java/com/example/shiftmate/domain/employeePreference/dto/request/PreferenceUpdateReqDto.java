package com.example.shiftmate.domain.employeePreference.dto.request;

import com.example.shiftmate.domain.employeePreference.entity.PreferenceType;
import lombok.Getter;

@Getter
public class PreferenceUpdateReqDto {

    private PreferenceType preferenceType;
}
