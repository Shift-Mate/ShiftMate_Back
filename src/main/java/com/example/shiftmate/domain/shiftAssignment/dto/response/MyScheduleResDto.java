package com.example.shiftmate.domain.shiftAssignment.dto.response;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MyScheduleResDto {
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String templateName;

    public static MyScheduleResDto from(ShiftAssignment shiftAssignment){
        return MyScheduleResDto.builder()
                   .workDate(shiftAssignment.getWorkDate())
                   .startTime(shiftAssignment.getShiftTemplate().getStartTime())
                   .endTime(shiftAssignment.getShiftTemplate().getEndTime())
                   .templateName(shiftAssignment.getShiftTemplate().getName())
                   .build();
    }
}
