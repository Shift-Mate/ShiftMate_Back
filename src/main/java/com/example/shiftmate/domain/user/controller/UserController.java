package com.example.shiftmate.domain.user.controller;

import com.example.shiftmate.domain.user.dto.request.UpdateMyProfileReqDto;
import com.example.shiftmate.domain.user.dto.response.*;
import com.example.shiftmate.domain.user.service.UserService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.shiftmate.domain.user.dto.request.ChangePasswordReqDto;
import jakarta.validation.Valid;

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

    @GetMapping("/admin/user-info")
    public ApiResponse<UserInfoResDto> getUserInfoForManager(
        @RequestParam String email,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return ApiResponse.success(userService.getUserInfoByEmailForManager(email, userDetails.getId()));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResDto> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.success(userService.getMyInfo(userDetails.getId()));
    }

    // [필터 API] 로그인 사용자가 근무한 기록이 있는 년/월 목록 조회
    @GetMapping("/me/salary/months")
    public ApiResponse<List<SalaryMonthResDto>> getMySalaryMonths(
            // 현재 로그인 사용자 정보(JWT) 주입
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에서 사용자 기준 월 목록 조회
        List<SalaryMonthResDto> result = userService.getMySalaryMonths(userDetails.getId());

        // 공통 성공 응답으로 반환
        return ApiResponse.success(result);
    }

    // [집계 API] 선택한 년/월의 스토어별 월급 집계 조회
    @GetMapping("/me/salary/monthly")
    public ApiResponse<MonthlySalarySummaryResDto> getMyMonthlySalary(
            // 조회할 연도 (예: 2025)
            @RequestParam int year,
            // 조회할 월 (1~12)
            @RequestParam int month,
            // 현재 로그인 사용자 정보(JWT) 주입
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에서 해당 년/월 집계 조회
        MonthlySalarySummaryResDto result =
                userService.getMyMonthlySalary(userDetails.getId(), year, month);

        // 공통 성공 응답으로 반환
        return ApiResponse.success(result);
    }

    // 로그인 사용자의 비밀번호 변경 API
    @PatchMapping("/me/password")
    public ApiResponse<String> changeMyPassword(
            // 요청 바디 유효성 검증
            @Valid @RequestBody ChangePasswordReqDto request,
            // 로그인 사용자 정보(JWT)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에 비밀번호 변경 위임
        userService.changeMyPassword(userDetails.getId(), request);

        // 성공 응답
        return ApiResponse.success("비밀번호가 변경되었습니다.");
    }

    // 로그인 사용자의 프로필(이름/전화번호) 수정 API
    @PatchMapping("/me")
    public ApiResponse<UserInfoResDto> updateMyProfile(
            // 요청 바디 유효성 검증
            @Valid @RequestBody UpdateMyProfileReqDto request,
            // 로그인 사용자 정보
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 서비스에서 수정 처리 후 최신 사용자 정보 반환
        UserInfoResDto result = userService.updateMyProfile(userDetails.getId(), request);

        // 공통 성공 응답
        return ApiResponse.success(result);
    }
}
