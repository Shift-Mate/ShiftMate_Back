package com.example.shiftmate.domain.employeePreference.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateWeeklyPreferenceReqDto {

    @NotEmpty(message = "선호도 목록은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<CreatePreferenceItemReqDto> preference;
}
