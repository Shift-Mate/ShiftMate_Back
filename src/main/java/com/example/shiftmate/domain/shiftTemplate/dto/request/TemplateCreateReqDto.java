package com.example.shiftmate.domain.shiftTemplate.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Getter;

@Getter
public class TemplateCreateReqDto {

    @NotNull(message = "피크 사용 여부는 필수입니다.")
    private Boolean peak;
    private LocalTime peakStartTime;
    private LocalTime peakEndTime;

    @AssertTrue(message = "피크를 사용하는 경우 시작/종료 시간은 필수이며 시작 시간이 종료 시간보다 빨라야 합니다.")
    public boolean isValidPeakTimeRange() {
        if (!Boolean.TRUE.equals(peak)) {
            return true;
        }

        if (peakStartTime == null || peakEndTime == null) {
            return false;
        }

        return peakStartTime.isBefore(peakEndTime);
    }

}
