package com.example.shiftmate.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class BiznoVerifyReqDto {

    @NotBlank(message = "사업자 번호는 필수입니다.")
    @Pattern(regexp = "\\d{3}-\\d{2}-\\d{5}", message = "사업자번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String bno;
}