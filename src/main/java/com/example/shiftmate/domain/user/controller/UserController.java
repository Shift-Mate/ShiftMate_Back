package com.example.shiftmate.domain.user.controller;


import com.example.shiftmate.domain.user.dto.response.MyStoreProfileResDto;
import com.example.shiftmate.domain.user.dto.response.MyStoreResDto;
import com.example.shiftmate.domain.user.service.UserService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// 사용자 관련 API 컨트롤러
@RestController
// final 필드 생성자 자동 생성
@RequiredArgsConstructor
// users 도메인 기본 경로
@RequestMapping("/users")
public class UserController {

    // 사용자 서비스 주입
    private final UserService userService;

    // 로그인한 사용자의 소속 스토어 목록 조회 API
    @GetMapping("/me/stores")
    public ApiResponse<List<MyStoreResDto>> getMyStores(
            // 현재 로그인 사용자 정보(JWT) 주입
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 로그인한 유저 id로 소속 스토어 목록 조회
        List<MyStoreResDto> result = userService.getMyStores(userDetails.getId());

        // 공통 성공 응답으로 반환
        return ApiResponse.success(result);
    }

    // 로그인한 사용자가 선택한 스토어에서의 내 상세 정보 조회 API
    @GetMapping("/me/stores/{storeId}/profile")
    public ApiResponse<MyStoreProfileResDto> getMyStoreProfile(
            // 선택한 스토어 ID를 경로에서 받음
            @PathVariable Long storeId,
            // 기준일(옵션), 없으면 서비스에서 오늘 날짜 사용
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate,
            // 로그인 사용자 정보 주입
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 로그인 유저 + 선택 스토어 기준으로 상세 정보 조회
        MyStoreProfileResDto result = userService.getMyStoreProfile(userDetails.getId(), storeId, baseDate);

        // 공통 성공 응답 반환
        return ApiResponse.success(result);
    }



}