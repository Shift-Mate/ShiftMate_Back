package com.example.shiftmate.domain.substitute.controller;

import com.example.shiftmate.domain.substitute.dto.request.SubstituteReqDto;
import com.example.shiftmate.domain.substitute.dto.response.SubstituteApplicationResDto;
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

    // 전체 대타 요청 내역 조회 (관리자)
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SubstituteResDto>>> getAllSubstitutes(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SubstituteResDto> responses = substituteService.getAllSubstitutes(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 본인 대타 요청 취소
    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubstitute(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.cancelSubstitute(storeId, userDetails.getId(), requestId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 대타 지원 생성
    @PostMapping("/{requestId}/apply")
    public ResponseEntity<ApiResponse<Void>> applySubstitute(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.createApplication(storeId, requestId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 본인 대타 지원 내역 조회
    @GetMapping("/applications/my")
    public ResponseEntity<ApiResponse<List<SubstituteApplicationResDto>>> getMyApplications(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SubstituteApplicationResDto> responses = substituteService.getMyApplications(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 대타 지원 취소
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<Void>> cancelApplication(
            @PathVariable Long storeId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.cancelApplication(storeId, applicationId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 특정 대타 요청에 대한 지원자 목록 조회 (관리자)
    @GetMapping("/{requestId}/applications")
    public ResponseEntity<ApiResponse<List<SubstituteApplicationResDto>>> getAllApplications(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<SubstituteApplicationResDto> responses = substituteService.getApplications(storeId, requestId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 대타 지원 승인 (관리자)
    @PatchMapping("/{requestId}/applications/{applicationId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveApplication(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.approveApplication(storeId, requestId, applicationId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 대타 지원 거절 (관리자)
    @PatchMapping("/{requestId}/applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectApplication(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.rejectApplication(storeId, requestId, applicationId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 대타 요청 취소 (관리자)
    @DeleteMapping("/{requestId}/manager-cancel")
    public ResponseEntity<ApiResponse<Void>> managerCancelRequest(
            @PathVariable Long storeId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        substituteService.managerCancelRequest(storeId, requestId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
