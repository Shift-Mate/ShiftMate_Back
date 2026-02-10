package com.example.shiftmate.domain.user.service;

import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.attendance.repository.AttendanceRepository;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.shiftAssignment.repository.ShiftAssignmentRepository;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.repository.StoreMemberRepository;
import com.example.shiftmate.domain.user.dto.response.WeeklyRemainingShiftsResDto;
import com.example.shiftmate.domain.user.dto.response.WeeklyWorkSummaryResDto;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AttendanceRepository attendanceRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;

    // 주간 실제 근무시간(분) 합산
    public long calculateActualMinutes(Long memberId, LocalDate weekStart, LocalDate weekEnd) {
        // 1) 주간 출퇴근 기록 조회
        List<Attendance> attendances =
                attendanceRepository.findAllByMemberIdAndWorkDateBetween(memberId, weekStart, weekEnd);

        // 2) clockIn~clockOut 시간 차이를 분으로 합산
        return attendances.stream()// 리스트를 하나씩 순회할 준비
                // 각 Attendance를 "근무 분"으로 변환
                .mapToLong(a -> Duration.between(a.getClockInAt(), a.getClockOutAt()).toMinutes())
                .sum(); //변환된 분들을 전부 더함
    }

    // 약속된 주간 근무시간(분) 반환 -> minHoursPerWeek 기준
    public long getPromisedMinutes(Long memberId) {
        // 1) StoreMember 조회 (없으면 에러)
        StoreMember member = storeMemberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 2) 주 단위 최소시간을 분 단위로 변환
        return member.getMinHoursPerWeek() * 60L;
    }


    // 주간 실제 근무시간 + 약속된 시간 요약 반환
    public WeeklyWorkSummaryResDto getWeeklyWorkSummary(Long memberId, LocalDate weekStart, LocalDate weekEnd) {
        // 1) 해당 멤버의 주간 실제 근무시간(분) 계산
        long actualMinutes = calculateActualMinutes(memberId, weekStart, weekEnd);

        // 2) 약속된 주간 근무시간(분) 조회 (minHoursPerWeek 기반)
        long promisedMinutes = getPromisedMinutes(memberId);

        // 3) 계산 결과를 DTO에 담아서 반환
        return WeeklyWorkSummaryResDto.builder()
                .weekStart(weekStart)          // 조회 시작일
                .weekEnd(weekEnd)              // 조회 종료일
                .actualMinutes(actualMinutes)  // 실제 근무시간 합계(분)
                .promisedMinutes(promisedMinutes) // 약속된 근무시간(분)
                .build();
    }


    // 주간 남은 근무 shift 개수 계산 (오늘 이후 + 미완료)
    public long calculateRemainingShifts(Long memberId, LocalDate weekStart, LocalDate weekEnd) {
        // 1) 해당 주간의 모든 스케줄 조회
        List<ShiftAssignment> assignments =
                shiftAssignmentRepository.findAllByMemberIdAndWorkDateBetween(memberId, weekStart, weekEnd);

        // 스케줄이 없으면 남은 shift도 없음
        if (assignments.isEmpty()) {
            return 0;
        }

        // 2) 스케줄에 연결된 근태 기록 조회 후 Map으로 변환
        Map<Long, Attendance> attendanceMap =
                attendanceRepository.findAllByShiftAssignmentIn(assignments).stream()
                        // key: shiftAssignmentId, value: Attendance
                        .collect(Collectors.toMap(a -> a.getShiftAssignment().getId(), a -> a));

        // 오늘 날짜 기준
        LocalDate today = LocalDate.now();

        // 3) 오늘 이후 + 아직 미완료(퇴근 기록 없음) 스케줄만 카운트
        return assignments.stream()
                // workDate가 오늘 이전이면 제외
                .filter(a -> !a.getWorkDate().isBefore(today))
                // attendance가 없거나, 있어도 clockOut이 없으면 "미완료"로 간주
                .filter(a -> {
                    Attendance att = attendanceMap.get(a.getId());
                    return att == null || att.getClockOutAt() == null;
                })
                // 남은 스케줄 개수 반환
                .count();
    }

    // 주간 남은 근무 shift 개수 반환
    public WeeklyRemainingShiftsResDto getWeeklyRemainingShifts(Long memberId, LocalDate weekStart, LocalDate weekEnd) {
        // 1) 주간 남은 shift 개수 계산
        long remainingShifts = calculateRemainingShifts(memberId, weekStart, weekEnd);

        // 2) 계산 결과를 DTO로 조립
        return WeeklyRemainingShiftsResDto.builder()
                .weekStart(weekStart)          // 조회 시작일
                .weekEnd(weekEnd)              // 조회 종료일
                .remainingShifts(remainingShifts) // 남은 shift 개수
                .build();
    }

}
