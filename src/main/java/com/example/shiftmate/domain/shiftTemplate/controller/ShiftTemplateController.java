package com.example.shiftmate.domain.shiftTemplate.controller;


import com.example.shiftmate.domain.shiftTemplate.dto.request.TemplateCreateReqDto;
import com.example.shiftmate.domain.shiftTemplate.dto.response.TemplateResDto;
import com.example.shiftmate.domain.shiftTemplate.service.ShiftTemplateService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;

    @PostMapping("/{storeId}/shift-template")
    public ResponseEntity<ApiResponse<List<TemplateResDto>>> createTemplate(
        @PathVariable Long storeId,
        @RequestBody @Valid TemplateCreateReqDto templateCreateReqDto
    ) {

        return ResponseEntity.ok(ApiResponse.success(
            shiftTemplateService.createTemplate(storeId, templateCreateReqDto)));
    }
}
