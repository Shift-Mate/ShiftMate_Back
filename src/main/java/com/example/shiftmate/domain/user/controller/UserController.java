package com.example.shiftmate.domain.user.controller;


import com.example.shiftmate.domain.user.dto.response.MyStoreResDto;
import com.example.shiftmate.domain.user.service.UserService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}