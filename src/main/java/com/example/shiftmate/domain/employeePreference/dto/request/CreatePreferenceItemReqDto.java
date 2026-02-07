package com.example.shiftmate.domain.employeePreference.dto.request;

import com.example.shiftmate.domain.employeePreference.entity.PreferenceType;
import java.time.DayOfWeek;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePreferenceItemReqDto {

    private DayOfWeek dayOfWeek;
    private PreferenceType type;
    private Long templateId;

}
