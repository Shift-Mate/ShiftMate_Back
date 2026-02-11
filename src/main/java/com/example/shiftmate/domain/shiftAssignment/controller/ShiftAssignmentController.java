package com.example.shiftmate.domain.shiftAssignment.controller;

import com.example.shiftmate.domain.shiftAssignment.dto.request.ScheduleCreateReqDto;
import com.example.shiftmate.domain.shiftAssignment.dto.response.MyScheduleResDto;
import com.example.shiftmate.domain.shiftAssignment.dto.response.ScheduleResDto;
import com.example.shiftmate.domain.shiftAssignment.service.ShiftAssignmentService;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.global.common.dto.ApiResponse;
import com.example.shiftmate.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{storeId}/schedules")
@RequiredArgsConstructor
public class ShiftAssignmentController {


    private final ShiftAssignmentService shiftAssignmentService;

    @PostMapping("/auto-generate")
    public ResponseEntity<ApiResponse<Void>> createSchedule(
        @PathVariable Long storeId,
        @Valid @RequestBody ScheduleCreateReqDto scheduleCreateReqDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        shiftAssignmentService.createSchedule(storeId, scheduleCreateReqDto.getWeekStartDate(),userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    // 날짜를 기준으로 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleResDto>>> getSchedule(
        @PathVariable Long storeId,
        @RequestParam LocalDate weekStartDate
    ){
        return ResponseEntity.ok(ApiResponse.success(shiftAssignmentService.getSchedule(storeId, weekStartDate)));
    }

    // 사용자를 기준으로 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<MyScheduleResDto>>> getSchedulesByMember(
        @PathVariable Long storeId,
        @PathVariable Long userId
    ){
        return ResponseEntity.ok(ApiResponse.success(shiftAssignmentService.getScheduleByMember(storeId,userId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
        @PathVariable Long storeId,
        @RequestParam LocalDate weekStartDate,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        shiftAssignmentService.deleteSchedule(storeId, weekStartDate,userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
