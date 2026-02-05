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

    // 다른 직원들의 대타 요청 조회
    @GetMapping("/others")
    public ResponseEntity<ApiResponse<List<SubstituteResDto>>> getOthersSubstitutes(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SubstituteResDto> responses = substituteService.getOthersSubstitutes(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 내 대타 요청 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<SubstituteResDto>>> getMySubstitutes(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SubstituteResDto> responses = substituteService.getMySubstitutes(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubstitute(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.cancelSubstitute(storeId, userDetails.getId(), requestId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
