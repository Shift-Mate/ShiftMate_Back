package com.example.shiftmate.domain.attendance.controller;

import com.example.shiftmate.domain.attendance.dto.request.AttendanceReqDto;
import com.example.shiftmate.domain.attendance.dto.response.AttendanceResDto;
import com.example.shiftmate.domain.attendance.dto.response.MyWeeklyAttendanceResDto;
import com.example.shiftmate.domain.attendance.dto.response.TodayAttendanceResDto;
import com.example.shiftmate.domain.attendance.dto.response.WeeklyAttendanceResDto;
import com.example.shiftmate.domain.attendance.service.AttendanceService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock")
    public ResponseEntity<ApiResponse<AttendanceResDto>> clock(
            @PathVariable Long storeId,
            @RequestBody @Valid AttendanceReqDto reqDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        AttendanceResDto response = attendanceService.processAttendance(storeId, reqDto, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<TodayAttendanceResDto>>> getAttendance(
            @PathVariable Long storeId,
            @RequestParam LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TodayAttendanceResDto> response = attendanceService.getTodayAttendance(storeId, date, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<List<WeeklyAttendanceResDto>>> getWeeklyAttendance(
            @PathVariable Long storeId,
            @RequestParam LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WeeklyAttendanceResDto> response = attendanceService.getWeeklyAttendance(storeId, date, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/daily/my")
    public ResponseEntity<ApiResponse<List<TodayAttendanceResDto>>> getMyAttendance(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TodayAttendanceResDto> response = attendanceService.getMyTodayAttendance(storeId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/weekly/my")
    public ResponseEntity<ApiResponse<MyWeeklyAttendanceResDto>> getMyWeeklyAttendance(
            @PathVariable Long storeId,
            @RequestParam LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MyWeeklyAttendanceResDto response = attendanceService.getMyWeeklyAttendance(storeId, date, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
