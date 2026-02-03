package com.example.shiftmate.domain.shiftTemplate.controller;


import com.example.shiftmate.domain.shiftTemplate.dto.request.TemplateCreateReqDto;
import com.example.shiftmate.domain.shiftTemplate.dto.request.TemplateShiftStaff;
import com.example.shiftmate.domain.shiftTemplate.dto.request.UpdateTemplateTypeReqDto;
import com.example.shiftmate.domain.shiftTemplate.dto.response.TemplateResDto;
import com.example.shiftmate.domain.shiftTemplate.entity.ShiftTemplate;
import com.example.shiftmate.domain.shiftTemplate.service.ShiftTemplateService;
import com.example.shiftmate.domain.store.entity.Store;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store/{storeId}/shift-template")
@RequiredArgsConstructor
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createTemplate(
        @PathVariable Long storeId,
        @RequestBody @Valid TemplateCreateReqDto templateCreateReqDto
    ) {
        shiftTemplateService.createTemplate(storeId, templateCreateReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PutMapping("{templateId}")
    public ResponseEntity<ApiResponse<TemplateResDto>> shiftStaff(
        @PathVariable Long templateId,
        @RequestBody @Valid TemplateShiftStaff templateShiftStaff
    ) {

        return ResponseEntity.ok(
            ApiResponse.success(shiftTemplateService.shiftStaff(templateId, templateShiftStaff)));
    }

    // TODO : 추후에 Store 엔티티로 옮겨야 함
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateTemplateType(
        @PathVariable Long storeId,
        @RequestBody @Valid UpdateTemplateTypeReqDto updateTemplateTypeReqDto
    ){
        shiftTemplateService.updateTemplateType(storeId , updateTemplateTypeReqDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 가게에 해당하는 모든 Shift 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResDto>>> getTemplate(
        @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(ApiResponse.success(shiftTemplateService.getTemplate(storeId)));
    }

    // 가게에서 template_type 이 동일한 shift 불러오기
}
