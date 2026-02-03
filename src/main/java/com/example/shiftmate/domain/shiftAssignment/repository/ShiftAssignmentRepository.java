package com.example.shiftmate.domain.shiftAssignment.repository;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment,Long> {
}
