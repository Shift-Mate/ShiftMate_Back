package com.example.shiftmate.domain.attendance.service;

import com.example.shiftmate.domain.attendance.dto.request.AttendanceReqDto;
import com.example.shiftmate.domain.attendance.dto.response.AttendanceResDto;
import com.example.shiftmate.domain.attendance.dto.response.TodayAttendanceResDto;
import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.attendance.entity.AttendanceStatus;
import com.example.shiftmate.domain.attendance.repository.AttendanceRepository;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public AttendanceResDto processAttendance(Long storeId, AttendanceReqDto reqDto) {
        // 스케줄 조회
        ShiftAssignment assignment = shiftAssignmentRepository.findById(reqDto.getAssignmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHIFT_ASSIGNMENT_NOT_FOUND));

        // 매장 일치 여부 및 핀번호 검증
        validateRequest(storeId, reqDto.getPinCode(), assignment);

        // 출근 기록이 있는지 확인
        Optional<Attendance> checkAttendance = attendanceRepository.findByShiftAssignment(assignment);

        if(checkAttendance.isEmpty()) { // 출근 기록이 없는 경우 -> 출근 처리
            return processClockIn(assignment);
        } else { // 출근 기록이 있는 경우
            Attendance attendance = checkAttendance.get();
            if(attendance.getClockOutAt() == null) { // 출근 기록은 있지만 퇴근 기록이 없음 -> 퇴근 처리
                return processClockOut(attendance);
            } else { // 퇴근 기록이 있음 -> 에러 처리
                throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_CLOSED);
            }
        }
    }

    // 검증 로직
    private void validateRequest(Long storeId, String pinCode, ShiftAssignment assignment) {
        // 해당 매장의 스케줄이 맞는지 확인
        // assignment의 근무자가 속한 매장의 id와 storeId가 일치하는지 확인
        // ShiftAssignment 엔티티에 store_id가 없어서 storeId와 일치하는 매장에 assignment가 있는지 확인하는 로직은 불가능
        if(!assignment.getMember().getStore().getId().equals(storeId)) {
            throw new CustomException(ErrorCode.STORE_MISMATCH);
        }

        // 핀번호가 해당 스케줄의 근무자의 핀번호와 일치하는지 확인
        // assignment의 근무자의 핀번호와 pinCode가 일치하는지 확인
        if(!assignment.getMember().getPinCode().equals(pinCode)) {
            throw new CustomException(ErrorCode.INVALID_PIN_CODE);
        }
    }

    // 출근 처리 로직
    private AttendanceResDto processClockIn(ShiftAssignment assignment) {
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status = AttendanceStatus.NORMAL;
        String message = "정상 출근 처리되었습니다.";

        if(now.isAfter(assignment.getUpdatedStartTime().plusMinutes(5))) {
            status = AttendanceStatus.LATE;
            message = "지각 처리되었습니다.";
        }

        Attendance newAttendance = Attendance.builder()
                .shiftAssignment(assignment)
                .clockInAt(now)
                .status(status)
                .build();

        attendanceRepository.save(newAttendance);

        return AttendanceResDto.builder()
                .attendanceId(newAttendance.getId())
                .status(status.name())
                .type("CLOCK_IN")
                .time(now)
                .message(message)
                .build();
    }

    // 퇴근 처리 로직
    private  AttendanceResDto processClockOut(Attendance attendance) {
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status = AttendanceStatus.OFFWORK;
        String message = "퇴근 처리되었습니다.";
        attendance.clockOut(now);

        return AttendanceResDto.builder()
                .attendanceId(attendance.getId())
                .status(status.name())
                .type("CLOCK_OUT")
                .time(now)
                .message(message)
                .build();
    }

    public List<TodayAttendanceResDto> getTodayAttendance(Long storeId, LocalDate date) {
        // 해당 매장에 해당 날짜에 존재하는 모든 스케줄 조회
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAllByStoreIdAndDate(storeId, date);

        // 스케줄이 없다면 빈 리스트 반환
        if(assignments.isEmpty()) {
            return List.of();
        }

        // 조회한 모든 스케줄에 연결된 출퇴근 기록 조회
        List<Attendance> attendances = attendanceRepository.findAllByShiftAssignmentIn(assignments);

        // 일일 근태 조회를 위한 리스트 생성
        List<TodayAttendanceResDto> results = new ArrayList<>();

        // 해당 배정 스케줄에 속한 attendance를 연결하는 로직
        for(ShiftAssignment assignment : assignments) {
            // matchAttendance가 null인 경우는 아직 출근하지 않았거나 결근한 상태라서 출퇴근 기록이 없는 상황
            // 마지막에 DTO 변환 시 null 처리
            Attendance matchAttendance = null;

            for(Attendance attendance : attendances) {
                // attendance의 배정 스케줄의 id와 해당 배정 스케줄의 id가 일치하는지 확인 후 연결
                if(attendance.getShiftAssignment().getId().equals(assignment.getId())) {
                    matchAttendance = attendance;
                    break;
                }
            }

            // 결과 리스트에 해당 assignment와 attendance를 연결해서 추가
            results.add(TodayAttendanceResDto.of(assignment, matchAttendance));
        }

        return results;
    }
}
