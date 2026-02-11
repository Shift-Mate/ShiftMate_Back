package com.example.shiftmate.domain.employeePreference.dto.request;

import com.example.shiftmate.domain.employeePreference.entity.PreferenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.DayOfWeek;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePreferenceItemReqDto {

    @NotNull(message = "요일은 필수입니다.")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "선호 타입은 필수입니다.")
    private PreferenceType type;

    @NotNull(message = "템플릿 ID는 필수입니다.")
    @Positive(message = "템플릿 ID는 1 이상이어야 합니다.")
    private Long templateId;

}
