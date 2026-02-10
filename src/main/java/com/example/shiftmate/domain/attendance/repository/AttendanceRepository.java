package com.example.shiftmate.domain.attendance.repository;

import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByShiftAssignment(ShiftAssignment shiftAssignment);

    // 여러 배정 스케줄에 속한 출근 기록을 한 번에 조회
    @EntityGraph(attributePaths = {"shiftAssignment"})
    List<Attendance> findAllByShiftAssignmentIn(List<ShiftAssignment> assignments);

    // 특정 멤버의 주간 실제 근무 기록 조회 (clockIn/clockOut 있는 것만)
    @Query("SELECT a FROM Attendance a " +
            "JOIN a.shiftAssignment sa " +
            "WHERE sa.member.id = :memberId AND sa.workDate BETWEEN :weekStart AND :weekEnd " +
            "AND a.clockInAt IS NOT NULL AND a.clockOutAt IS NOT NULL")
    List<Attendance> findAllByMemberIdAndWorkDateBetween(
            @Param("memberId") Long memberId,
            @Param("weekStart") LocalDate weekStart, // 시간 계산해서 보여줄 그 주의 시작일
            @Param("weekEnd") LocalDate weekEnd // 주의 마지막일
    );

}
