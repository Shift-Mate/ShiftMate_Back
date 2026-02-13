package com.example.shiftmate.domain.store.controller;

import com.example.shiftmate.domain.store.dto.request.BiznoVerifyReqDto;
import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.BiznoVerifyResDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.service.StoreService;
import com.example.shiftmate.domain.store.service.BiznoService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final BiznoService biznoService;

    // store create (사용자가 매장 생성자로 등록됨)
    @PostMapping
    public ResponseEntity<ApiResponse<StoreResDto>> createStore(
            @Valid @RequestBody StoreReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StoreResDto response = storeService.create(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 단일 매장 조회, GET /stores/{storeId}
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResDto>> getStore(@PathVariable Long storeId){
        StoreResDto response = storeService.findById(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그인한 유저가 속한 매장 목록 조회, GET /stores
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResDto>>> getAllStores(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<StoreResDto> response = storeService.findStoresByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 유저가 속해있는 매장 수정(생성자만 수정)
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResDto>> updateStore(
        @PathVariable Long storeId,
        @Valid @RequestBody StoreReqDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        StoreResDto response = storeService.update(storeId, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Store Delete, DELETE /stores/{storeId}
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
        @PathVariable Long storeId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        storeService.delete(storeId, userDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null));
    }

    // 사업자 번호 검증, POST /stores/verify-bizno
    @PostMapping("/verify-bizno")
    public ResponseEntity<ApiResponse<BiznoVerifyResDto>> verifyBusinessNumber(
        @Valid @RequestBody BiznoVerifyReqDto request
    ) {
        BiznoVerifyResDto response = biznoService.verifyBusinessNumber(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
