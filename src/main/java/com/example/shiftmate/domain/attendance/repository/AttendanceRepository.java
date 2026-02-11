package com.example.shiftmate.domain.attendance.repository;

import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByShiftAssignment(ShiftAssignment shiftAssignment);

    // 여러 배정 스케줄에 속한 출근 기록을 한 번에 조회
    @EntityGraph(attributePaths = {"shiftAssignment"})
    List<Attendance> findAllByShiftAssignmentIn(List<ShiftAssignment> assignments);


    // 특정 멤버의 특정 기간(주간 등) 실제 근무 기록 조회
    @Query("SELECT a FROM Attendance a " + // Attendance 엔티티 조회 시작
            "JOIN a.shiftAssignment sa " + // 근무배정(ShiftAssignment)과 조인
            "WHERE sa.member.id = :memberId " + // 특정 멤버 조건
            "AND sa.workDate BETWEEN :startDate AND :endDate " + // 기간 조건
            "AND a.clockInAt IS NOT NULL AND a.clockOutAt IS NOT NULL") // 출퇴근 완료 기록만 조회
    List<Attendance> findAllByMemberIdAndWorkDateBetween( // 메서드 선언 시작
                                                          @Param("memberId") Long memberId, // 조회 대상 멤버 ID
                                                          @Param("startDate") LocalDate startDate, // 조회 시작일
                                                          @Param("endDate") LocalDate endDate // 조회 종료일
    );
}
