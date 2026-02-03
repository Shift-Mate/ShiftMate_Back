package com.example.shiftmate.domain.attendance.controller;

import com.example.shiftmate.domain.attendance.dto.request.AttendanceReqDto;
import com.example.shiftmate.domain.attendance.dto.response.AttendanceResDto;
import com.example.shiftmate.domain.attendance.service.AttendanceService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock")
    public ResponseEntity<ApiResponse<AttendanceResDto>> clock(
            @PathVariable Long storeId,
            @RequestBody @Valid AttendanceReqDto reqDto
    ) {
        AttendanceResDto response = attendanceService.processAttendance(storeId, reqDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
