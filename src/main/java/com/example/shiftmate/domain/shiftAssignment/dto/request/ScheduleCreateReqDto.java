package com.example.shiftmate.domain.shiftAssignment.dto.request;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class ScheduleCreateReqDto {

    // 프론트에서 월요일인지 확인한 후에 요청을 보내야 함
    private LocalDate weekStartDate;
}
