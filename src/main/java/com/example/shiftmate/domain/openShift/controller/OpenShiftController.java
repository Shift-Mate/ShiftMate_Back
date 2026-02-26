package com.example.shiftmate.domain.openShift.controller;

import com.example.shiftmate.domain.openShift.dto.request.OpenShiftReqDto;
import com.example.shiftmate.domain.openShift.dto.response.OpenShiftApplyResDto;
import com.example.shiftmate.domain.openShift.dto.response.OpenShiftResDto;
import com.example.shiftmate.domain.openShift.service.OpenShiftService;
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
@RequestMapping("/stores/{storeId}/open-shift")
public class OpenShiftController {
    private final OpenShiftService openShiftService;

    // 오픈시프트 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createOpenShift(
            @PathVariable Long storeId,
            @RequestBody @Valid OpenShiftReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        openShiftService.createOpenShift(storeId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 오픈시프트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<OpenShiftResDto>>> getOpenShifts(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<OpenShiftResDto> response = openShiftService.getOpenShifts(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 오픈시프트 지원
    @PostMapping("/{openShiftId}/apply")
    public ResponseEntity<ApiResponse<Void>> applyOpenShift(
            @PathVariable Long storeId,
            @PathVariable Long openShiftId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        openShiftService.applyOpenShift(storeId, openShiftId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 나의 오픈시프트 지원 내역 조회
    @GetMapping("/my-applies")
    public ResponseEntity<ApiResponse<List<OpenShiftApplyResDto>>> getMyOpenShiftApplies(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<OpenShiftApplyResDto> response = openShiftService.getMyOpenShiftApplies(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 오픈시프트 지원자 목록 조회
    @GetMapping("/{openShiftId}/applies")
    public ResponseEntity<ApiResponse<List<OpenShiftApplyResDto>>> getOpenShiftApplies(
            @PathVariable Long storeId,
            @PathVariable Long openShiftId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<OpenShiftApplyResDto> response = openShiftService.getOpenShiftApplies(storeId, openShiftId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 오픈시프트 지원 승인
    @PatchMapping("/{openShiftId}/{applyId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveOpenShift(
            @PathVariable Long storeId,
            @PathVariable Long openShiftId,
            @PathVariable Long applyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        openShiftService.approveApply(storeId, openShiftId, applyId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
