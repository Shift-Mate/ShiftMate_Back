package com.example.shiftmate.domain.employeePreference.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateWeeklyPreferenceReqDto {

    private List<CreatePreferenceItemReqDto> preference;
}
