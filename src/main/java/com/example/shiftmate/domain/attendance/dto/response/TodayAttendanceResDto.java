package com.example.shiftmate.domain.attendance.dto.response;

import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.attendance.entity.AttendanceStatus;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TodayAttendanceResDto {

    // 스케줄 정보
    private Long assignmentId;
    private LocalDateTime updatedStartTime; // 출근 시간
    private LocalDateTime updatedEndTime; // 퇴근 시간

    // 근무자 정보
    private String workerName; // 근무자 이름
    private StoreRole role; // 근무자 역할
    private Department department; // 근무자 부서

    // 출퇴근 상태
    private LocalDateTime clockInAt; // 실제 출근 시간
    private LocalDateTime clockOutAt; // 실제 퇴근 시간
    private AttendanceStatus status; // 출퇴근 상태

    public static TodayAttendanceResDto of(ShiftAssignment assignment, Attendance attendance) {
        return TodayAttendanceResDto.builder()
                .assignmentId(assignment.getId())
                .updatedStartTime(assignment.getUpdatedStartTime())
                .updatedEndTime(assignment.getUpdatedEndTime())
                .workerName(assignment.getMember().getUser().getName())
                .role(assignment.getMember().getRole())
                .department(assignment.getMember().getDepartment())
                // 출퇴근 기록을 남기지 않은 스케줄도 있을 수 있으므로 attendance가 null이라면 null처리
                .clockInAt(attendance != null ? attendance.getClockInAt() : null)
                .clockOutAt(attendance != null ? attendance.getClockOutAt() : null)
                .status(attendance != null ? attendance.getStatus() : null)
                .build();
    }
}
