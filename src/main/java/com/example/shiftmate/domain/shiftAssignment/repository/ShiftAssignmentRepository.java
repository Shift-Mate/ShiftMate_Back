package com.example.shiftmate.domain.shiftAssignment.repository;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment,Long> {

    // 특정 매장의 특정 날짜의 배정된 스케줄 및 직원 정보 조회
    @Query("SELECT sa FROM ShiftAssignment sa " +
            "JOIN FETCH sa.member m " +
            "JOIN FETCH sa.shiftTemplate " +
            "JOIN FETCH m.user " +
            "WHERE m.store.id = :storeId AND sa.workDate = :date")
    List<ShiftAssignment> findAllByStoreIdAndDate(@Param("storeId")Long storeId, @Param("date") LocalDate date);

    // 특정 근무자의 스케줄 중 요청된 시간 범위와 겹치는 스케줄이 있는지 확인
    // 조건: (기존 시작 < 요청 종료) AND (기존 종료 > 요청 시작) -> 교집합이 존재함
    @Query("SELECT COUNT(sa) > 0 FROM ShiftAssignment sa " +
            "WHERE sa.member.id = :memberId " +
            "AND sa.workDate = :workDate " +
            "AND sa.updatedStartTime < :endTime " +
            "AND sa.updatedEndTime > :startTime")
    boolean existsByMemberIdAndWorkDateAndOverlapTime(
            @Param("memberId") Long memberId,
            @Param("workDate") LocalDate workDate,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
