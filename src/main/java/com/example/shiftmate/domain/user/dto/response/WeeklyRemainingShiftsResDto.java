package com.example.shiftmate.domain.user.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyRemainingShiftsResDto {

    // 주간 시작일
    private LocalDate weekStart;

    // 주간 종료일
    private LocalDate weekEnd;

    // 남은 근무 shift 개수 (해당 주간 기준)
    private long remainingShifts;
}
