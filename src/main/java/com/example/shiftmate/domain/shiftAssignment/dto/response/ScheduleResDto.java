package com.example.shiftmate.domain.shiftAssignment.dto.response;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScheduleResDto {


    // 직원 정보
    private String memberName;

    // 스케줄 정보
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // 템플릿 정보
    private String templateName;

    public static ScheduleResDto from (ShiftAssignment shiftAssignment){
        return ScheduleResDto.builder()
                   .memberName(shiftAssignment.getMember().getUser().getName())
                   .workDate(shiftAssignment.getWorkDate())
                   .startTime(shiftAssignment.getShiftTemplate().getStartTime())
                   .endTime(shiftAssignment.getShiftTemplate().getEndTime())
                   .templateName(shiftAssignment.getShiftTemplate().getName())
                   .build();
    }
}
