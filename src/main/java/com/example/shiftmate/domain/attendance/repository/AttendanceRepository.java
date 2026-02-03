package com.example.shiftmate.domain.attendance.repository;

import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByShiftAssignment(ShiftAssignment shiftAssignment);

    // 여러 배정 스케줄에 속한 출근 기록을 한 번에 조회
    @Query("SELECT a FROM Attendance a WHERE a.shiftAssignment IN :assignments")
    List<Attendance> findAllByShiftAssignmentIn(@Param("assignments")List<ShiftAssignment> assignments);
}
