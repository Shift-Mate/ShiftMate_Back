package com.example.shiftmate.domain.user.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

//Response DTO는 서버가 반환하는 결과값을 담는 용도
//Response: 보여줄 데이터만 담는 출력 스펙
@Getter
@Builder
public class WeeklyWorkSummaryResDto {

    // 주간 시작일
    private LocalDate weekStart;

    // 주간 종료일
    private LocalDate weekEnd;

    // 실제 근무 총 시간(분 단위)
    private long actualMinutes;

    // 약속된 주간 근무 시간(분 단위) -> minHoursPerWeek 기준
    private long promisedMinutes;

}