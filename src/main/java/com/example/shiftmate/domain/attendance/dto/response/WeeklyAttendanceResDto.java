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

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyAttendanceResDto {
    private Long assignmentId;

    // 근무자 정보
    private String workerName; // 근무자 이름
    private StoreRole role; // 근무자 역할
    private Department department; // 근무자 부서

    // 스케줄 정보
    private LocalDateTime updatedStartTime; // 출근 시간
    private LocalDateTime updatedEndTime; // 퇴근 시간

    // 출퇴근 상태
    private LocalDateTime clockInAt; // 실제 출근 시간
    private LocalDateTime clockOutAt; // 실제 퇴근 시간
    private AttendanceStatus status; // 출퇴근 상태

    // 근무시간(분)
    private Long workedMinutes;

    // 대타 요청 신청 여부 확인
    private boolean hasSubstituteRequest;

    public static WeeklyAttendanceResDto of(ShiftAssignment assignment, Attendance attendance) {
        return of(assignment, attendance, false);
    }

    public static WeeklyAttendanceResDto of(ShiftAssignment assignment, Attendance attendance, boolean hasSubstituteRequest) {
        long minutes = 0;
        if(attendance != null && attendance.getClockInAt() != null && attendance.getClockOutAt() != null) {
            minutes = Duration.between(attendance.getClockInAt(), attendance.getClockOutAt()).toMinutes();
        }
        return WeeklyAttendanceResDto.builder()
                .assignmentId(assignment.getId())
                .workerName(assignment.getMember().getUser().getName())
                .role(assignment.getMember().getRole())
                .department(assignment.getMember().getDepartment())
                .updatedStartTime(assignment.getUpdatedStartTime())
                .updatedEndTime(assignment.getUpdatedEndTime())
                .clockInAt(attendance != null ? attendance.getClockInAt() : null)
                .clockOutAt(attendance != null ? attendance.getClockOutAt() : null)
                .status(attendance != null ? attendance.getStatus() : null)
                .workedMinutes(minutes)
                .hasSubstituteRequest(hasSubstituteRequest)
                .build();
    }
}
