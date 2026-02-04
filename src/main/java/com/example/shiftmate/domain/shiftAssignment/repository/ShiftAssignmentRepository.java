package com.example.shiftmate.domain.shiftAssignment.repository;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment,Long> {

    // 특정 매장의 특정 날짜의 배정된 스케줄 및 직원 정보 조회
    @Query("SELECT sa FROM ShiftAssignment sa " +
            "JOIN FETCH sa.member m " +
            "JOIN FETCH sa.shiftTemplate " +
            "JOIN FETCH m.user " +
            "WHERE m.store.id = :storeId AND sa.workDate = :date")
    List<ShiftAssignment> findAllByStoreIdAndDate(@Param("storeId")Long storeId, @Param("date") LocalDate date);
}
