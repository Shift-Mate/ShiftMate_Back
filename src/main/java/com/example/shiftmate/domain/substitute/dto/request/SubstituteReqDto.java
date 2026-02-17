package com.example.shiftmate.domain.substitute.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubstituteReqDto {

    @NotNull
    private Long assignmentId;

    @Size(max = 200, message = "사유는 200자 이내로 입력해주세요.")
    private String reason;
}
