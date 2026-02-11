package com.example.shiftmate.domain.shiftTemplate.dto.request;

import com.example.shiftmate.domain.shiftTemplate.entity.TemplateType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateTemplateTypeReqDto {

    @NotNull(message = "템플릿 타입은 필수입니다.")
    private TemplateType templateType;
}
