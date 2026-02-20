package com.example.shiftmate.domain.shiftTemplate.dto.response;

import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftType;
import com.example.shiftmate.domain.shiftTemplate.entity.TemplateType;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TemplateResDto {

    private TemplateType templateType;
    private ShiftType shiftType;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;

    public static TemplateResDto from(ShiftTemplate shiftTemplate) {
        return TemplateResDto.builder()
                   .templateType(shiftTemplate.getTemplateType())
                   .shiftType(shiftTemplate.getShiftType())
                   .name(shiftTemplate.getName())
                   .startTime(shiftTemplate.getStartTime())
                   .endTime(shiftTemplate.getEndTime())
                   .build();
    }
}
