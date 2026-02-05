package com.example.shiftmate.domain.substitute.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubstituteReqDto {

    @NotNull
    private Long assignmentId;

    private String reason;
}
