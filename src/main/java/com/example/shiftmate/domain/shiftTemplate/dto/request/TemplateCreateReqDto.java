package com.example.shiftmate.domain.shiftTemplate.dto.request;

import java.time.LocalTime;
import lombok.Getter;

@Getter
public class TemplateCreateReqDto {

    private Boolean peak;
    private LocalTime peakStartTime;
    private LocalTime peakEndTime;

}
