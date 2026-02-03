package com.example.shiftmate.domain.store.controller;

import com.example.shiftmate.domain.store.dto.request.StoreReqDto;
import com.example.shiftmate.domain.store.dto.response.StoreResDto;
import com.example.shiftmate.domain.store.service.StoreService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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


}
