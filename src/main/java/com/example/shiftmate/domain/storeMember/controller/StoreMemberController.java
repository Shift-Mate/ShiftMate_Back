package com.example.shiftmate.domain.storeMember.controller;

import com.example.shiftmate.domain.storeMember.dto.request.StoreMemberReqDto;
import com.example.shiftmate.domain.storeMember.dto.response.StoreMemberResDto;
import com.example.shiftmate.domain.storeMember.service.StoreMemberService;
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
@RequestMapping("/store-members")
@RequiredArgsConstructor
public class StoreMemberController {

    private final StoreMemberService storeMemberService;

    // StoreMember create
    @PostMapping
    public ResponseEntity<ApiResponse<StoreMemberResDto>> createStoreMember(
        @Valid @RequestBody StoreMemberReqDto request
        // todo 임시: 인증(secu, jwt) 구현 후 수정 필요
        // @CurrentUserId Long userId) 으로 수정
    ) {
        StoreMemberResDto response = storeMemberService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

}
