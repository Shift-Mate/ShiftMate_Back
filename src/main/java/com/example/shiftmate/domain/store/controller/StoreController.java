package com.example.shiftmate.domain.store.controller;

import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.service.StoreService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // store create
    @PostMapping
    public ResponseEntity<ApiResponse<StoreResDto>> createStore(
            @Valid @RequestBody StoreReqDto request
            // todo 임시: 인증(secu, jwt) 구현 후 수정 필요
            // @CurrentUserId Long userId) 으로 수정
            // @RequestHeader("X-User-Id") Long userId
    ) {
        StoreResDto response = storeService.create(request, 1L); // 임시: userId 하드코딩
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 단일 매장 조회, GET /stores/{storeId}
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResDto>> getStore(@PathVariable Long storeId){
        StoreResDto response = storeService.findById(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 전체 매장 목록 조회(테스트용), GET /stores
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResDto>>> getAllStores(){
        List<StoreResDto> response = storeService.findAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 유저가 속해있는 매장 조회
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResDto>> updateStore(
        @PathVariable Long storeId,
        @Valid @RequestBody StoreReqDto request
        // todo 임시: 인증(secu, jwt) 구현 후 수정 필요
        // @CurrentUserId Long userId) 으로 수정
    ){
        StoreResDto response = storeService.update(storeId, request, 1L);
        return ResponseEntity.ok(ApiResponse.success(response));

    }

    // Store Delete, DELETE /stores/{storeId}
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
        @PathVariable Long storeId
        // todo 임시: 인증(secu, jwt) 구현 후 수정 필요
        // @CurrentUserId Long userId) 으로 수정
    ){
        storeService.delete(storeId, 1L); // 임시: userId 하드코딩
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(null));
    }
}
