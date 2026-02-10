package com.example.shiftmate.domain.user.controller;

import com.example.shiftmate.domain.user.dto.response.WeeklyRemainingShiftsResDto;
import com.example.shiftmate.domain.user.dto.response.WeeklyWorkSummaryResDto;
import com.example.shiftmate.domain.user.service.UserService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 사용자 관련 API 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 주간 근무 요약 조회 API
    @GetMapping("/{memberId}/weekly-work-summary")
    public ApiResponse<WeeklyWorkSummaryResDto> getWeeklyWorkSummary(
            @PathVariable Long memberId,
            // weekStart를 yyyy-MM-dd 형식으로 받음
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            // weekEnd를 yyyy-MM-dd 형식으로 받음
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd
    ) {
        WeeklyWorkSummaryResDto result = userService.getWeeklyWorkSummary(memberId, weekStart, weekEnd);
        return ApiResponse.success(result);
    }

    // 주간 남은 근무 shift 개수 조회 API
    @GetMapping("/{memberId}/weekly-remaining-shifts")
    public ApiResponse<WeeklyRemainingShiftsResDto> getWeeklyRemainingShifts(
            @PathVariable Long memberId,
            // weekStart를 yyyy-MM-dd 형식으로 받음
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            // weekEnd를 yyyy-MM-dd 형식으로 받음
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd
    ) {
        WeeklyRemainingShiftsResDto result = userService.getWeeklyRemainingShifts(memberId, weekStart, weekEnd);
        return ApiResponse.success(result);
    }
}