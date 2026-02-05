package com.example.shiftmate.domain.substitute.controller;

import com.example.shiftmate.domain.substitute.dto.request.SubstituteReqDto;
import com.example.shiftmate.domain.substitute.dto.response.SubstituteResDto;
import com.example.shiftmate.domain.substitute.service.SubstituteService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/substitute-requests")
public class SubstituteController {

    private final SubstituteService substituteService;

    // 대타 요청 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createSubstitute(
            @PathVariable Long storeId,
            @RequestBody @Valid SubstituteReqDto reqDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.createSubstitute(storeId, reqDto, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
