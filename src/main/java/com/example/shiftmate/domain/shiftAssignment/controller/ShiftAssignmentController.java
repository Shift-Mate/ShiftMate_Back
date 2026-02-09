package com.example.shiftmate.domain.shiftAssignment.controller;

import com.example.shiftmate.domain.shiftAssignment.dto.request.ScheduleCreateReqDto;
import com.example.shiftmate.domain.shiftAssignment.dto.response.ScheduleResDto;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.shiftAssignment.service.ShiftAssignmentService;
import com.example.shiftmate.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stores/{storeId}")
@RequiredArgsConstructor
public class ShiftAssignmentController {


    private final ShiftAssignmentService shiftAssignmentService;

    @PostMapping("/schedules/auto-generate")
    public ResponseEntity<ApiResponse<Void>> createSchedule(
        @PathVariable Long storeId,
        @Valid @RequestBody ScheduleCreateReqDto scheduleCreateReqDto
    ) {
        shiftAssignmentService.createSchedule(storeId, scheduleCreateReqDto.getWeekStartDate());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
