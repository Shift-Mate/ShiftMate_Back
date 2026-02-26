package com.example.shiftmate.domain.openShift.dto.request;

import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class OpenShiftReqDto {
    @NotNull(message = "근무 파트는 필수입니다.")
    private Long shiftTemplateId;
    @NotNull(message = "근무 날짜는 필수입니다.")
    private LocalDate workDate;
    private String note; // 해당 오픈시프트에 대한 참고사항
}
