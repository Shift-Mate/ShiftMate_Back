package com.example.shiftmate.domain.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyWeeklyAttendanceResDto {

    private String totalWorkTime;
    private long totalMinutes;
    private List<WeeklyAttendanceResDto> weeklyData;

    public static MyWeeklyAttendanceResDto of(List<WeeklyAttendanceResDto> weeklyData) {

        long sumMinutes = weeklyData.stream()
                .mapToLong(dto -> dto.getWorkedMinutes() != null ? dto.getWorkedMinutes() : 0L)
                .sum();

        long hours = sumMinutes / 60;
        long minutes = sumMinutes % 60;
        String formattedTime = String.format("%d시간 %d분", hours, minutes);

        return MyWeeklyAttendanceResDto.builder()
                .totalWorkTime(formattedTime)
                .totalMinutes(sumMinutes)
                .weeklyData(weeklyData)
                .build();
    }
}