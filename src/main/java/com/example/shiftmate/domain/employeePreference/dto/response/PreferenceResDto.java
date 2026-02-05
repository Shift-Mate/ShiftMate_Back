package com.example.shiftmate.domain.employeePreference.dto.response;

import com.example.shiftmate.domain.employeePreference.entity.EmployeePreference;
import com.example.shiftmate.domain.employeePreference.entity.PreferenceType;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreferenceResDto {

    private Integer dayOfWeek;
    private String name;
    private PreferenceType preferenceType;
    private LocalTime startTime;
    private LocalTime endTime;

    public static PreferenceResDto from(EmployeePreference employeePreference){
        return PreferenceResDto.builder()
                   .dayOfWeek(employeePreference.getDayOfWeek())
                   .name(employeePreference.getShiftTemplate().getName())
                   .preferenceType(employeePreference.getType())
                   .startTime(employeePreference.getShiftTemplate().getStartTime())
                   .endTime(employeePreference.getShiftTemplate().getEndTime())
                   .build();
    }
}
