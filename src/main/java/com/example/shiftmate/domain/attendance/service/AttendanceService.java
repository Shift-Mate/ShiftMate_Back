package com.example.shiftmate.domain.attendance.service;

import com.example.shiftmate.domain.attendance.dto.request.AttendanceReqDto;
import com.example.shiftmate.domain.attendance.dto.response.AttendanceResDto;
import com.example.shiftmate.domain.attendance.dto.response.TodayAttendanceResDto;
import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.attendance.entity.AttendanceStatus;
import com.example.shiftmate.domain.attendance.repository.AttendanceRepository;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.entity.StoreRank;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final StoreMemberRepository storeMemberRepository;

    private static final ZoneId KST_TIME = ZoneId.of("Asia/Seoul");

    @Transactional
    public AttendanceResDto processAttendance(Long storeId, AttendanceReqDto reqDto, Long userId) {
        // 관리자 화면에서만 출퇴근 체크가 가능
        // -> 요청자가 해당 매장의 관리자인지 조회
        StoreMember member = storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND));

        if(member.getMemberRank() != StoreRank.MANAGER) {
            throw new CustomException(ErrorCode.NOT_AUTHORIZED);
        }

        // 스케줄 조회
        ShiftAssignment assignment = shiftAssignmentRepository.findById(reqDto.getAssignmentId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHIFT_ASSIGNMENT_NOT_FOUND));

        // 출근 기록이 있는지 확인
        Optional<Attendance> checkAttendance = attendanceRepository.findByShiftAssignment(assignment);
        boolean isClockIn = checkAttendance.isEmpty();
        LocalDateTime now = LocalDateTime.now(KST_TIME);

        // 매장 일치 여부 및 핀번호 검증
        validateRequest(storeId, reqDto.getPinCode(), assignment, isClockIn);

        if(isClockIn) { // 출근 기록이 없는 경우 -> 출근 처리
            return processClockIn(assignment);
        } else { // 출근 기록이 있는 경우
            Attendance attendance = checkAttendance.get();
            if(attendance.getClockOutAt() == null) { // 출근 기록은 있지만 퇴근 기록이 없음 -> 퇴근 처리
                // 출근 요청 버튼을 연속 클릭하면 출근 후 바로 퇴근 처리가 될 수 있음
                // 출근 처리 후 5분이 지난 시점부터 퇴근 처리 가능하도록 조건 설정
                long minutesElapsed = java.time.Duration.between(attendance.getClockInAt(), now).toMinutes();

                if (minutesElapsed < 5) {
                    // 5분 미만이면 에러 발생
                    throw new CustomException(ErrorCode.TOO_FAST_CLOCK_OUT);
                }
                return processClockOut(attendance);
            } else { // 퇴근 기록이 있음 -> 에러 처리
                throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_CLOSED);
            }
        }
    }

    // 검증 로직
    private void validateRequest(Long storeId, String pinCode, ShiftAssignment assignment, boolean isClockIn) {
        LocalDateTime now = LocalDateTime.now(KST_TIME);
        LocalDateTime start = assignment.getUpdatedStartTime();
        LocalDateTime end = assignment.getUpdatedEndTime();

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

        if(isClockIn) {
            // 출근 요청은 출근 시간 30분 전부터 근무 종료 시간까지 가능
            // 이 조건이 없으면 출근 시간이 한참 남은 시간에도 출근 처리가 가능하므로 조건 설정
            if(now.isBefore(start.minusMinutes(30)) || now.isAfter(end)) {
                throw new CustomException(ErrorCode.NOT_CLOCK_IN_TIME);
            }
        } else {
            // 퇴근 요청은 출근 시간부터 근무 종료 시간 4시간 후까지 가능
            // 조기 퇴근을 할 수 있고, 연장 근무가 있을 수 있으므로 조건 설정
            if(now.isBefore(start) || now.isAfter(end.plusHours(4))) {
                throw new CustomException(ErrorCode.NOT_CLOCK_OUT_TIME);
            }
        }
    }

    // 출근 처리 로직
    private AttendanceResDto processClockIn(ShiftAssignment assignment) {
        LocalDateTime now = LocalDateTime.now(KST_TIME);
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
        LocalDateTime now = LocalDateTime.now(KST_TIME);
        AttendanceStatus status = AttendanceStatus.OFFWORK;
        String message = "퇴근 처리되었습니다.";
        attendance.clockOut(now);
        attendance.changeStatus(status);

        return AttendanceResDto.builder()
                .attendanceId(attendance.getId())
                .status(status.name())
                .type("CLOCK_OUT")
                .time(now)
                .message(message)
                .build();
    }

    public List<TodayAttendanceResDto> getTodayAttendance(Long storeId, LocalDate date, Long userId) {
        // 해당 매장의 멤버인지 검증
        storeMemberRepository.findByStoreIdAndUserId(storeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_MEMBER_NOT_FOUND));

        // 해당 매장에 해당 날짜에 존재하는 모든 스케줄 조회
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAllByStoreIdAndDate(storeId, date);

        // 스케줄이 없다면 빈 리스트 반환
        if(assignments.isEmpty()) {
            return List.of();
        }

        // 조회한 모든 스케줄에 연결된 출퇴근 기록 조회
        List<Attendance> attendances = attendanceRepository.findAllByShiftAssignmentIn(assignments);

        // 해당 배정 스케줄에 속한 attendance를 연결하는 로직
        Map<Long, Attendance> attendanceMap = attendances.stream()
                .collect(Collectors.toMap(
                        attendance -> attendance.getShiftAssignment().getId(),
                        attendance -> attendance
                ));

        return assignments.stream()
                .map(assignment -> TodayAttendanceResDto.of(assignment, attendanceMap.get(assignment.getId())))
                .collect(Collectors.toList());
    }
}
