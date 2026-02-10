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

    // 특정 멤버의 주간 배정 스케줄을 조회하는 JPQL 쿼리
    @Query("SELECT sa FROM ShiftAssignment sa " +          // ShiftAssignment 엔티티를 조회
            "WHERE sa.member.id = :memberId " +           // 특정 멤버(memberId)에 해당하는 스케줄만 필터
            "AND sa.workDate BETWEEN :weekStart AND :weekEnd") // 주간 범위(weekStart~weekEnd)만 조회
    List<ShiftAssignment> findAllByMemberIdAndWorkDateBetween(
            @Param("memberId") Long memberId,                // 조회할 멤버의 id 파라미터
            @Param("weekStart") LocalDate weekStart,         // 조회 시작 날짜
            @Param("weekEnd") LocalDate weekEnd              // 조회 종료 날짜
    );



}
