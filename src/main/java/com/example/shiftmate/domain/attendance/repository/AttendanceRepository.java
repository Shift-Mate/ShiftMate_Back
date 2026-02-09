package com.example.shiftmate.domain.attendance.repository;

import com.example.shiftmate.domain.attendance.entity.Attendance;
import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByShiftAssignment(ShiftAssignment shiftAssignment);

    // 여러 배정 스케줄에 속한 출근 기록을 한 번에 조회
    @EntityGraph(attributePaths = {"shiftAssignment"})
    List<Attendance> findAllByShiftAssignmentIn(List<ShiftAssignment> assignments);
}
