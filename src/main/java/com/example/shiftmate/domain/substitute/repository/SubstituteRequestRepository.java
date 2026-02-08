package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubstituteRequestRepository extends JpaRepository<SubstituteRequest, Long> {

    boolean existsByShiftAssignmentAndStatusIn(ShiftAssignment shiftAssignment, List<RequestStatus> statuses);

    // 본인의 대타 요청 조회
    @Query("SELECT r FROM SubstituteRequest r " +
            "JOIN FETCH r.requester m " +      // StoreMember 조인
            "JOIN FETCH m.user " +             // User 조인 (이름 조회용)
            "JOIN FETCH r.shiftAssignment sa " + // Assignment 조인
            "JOIN FETCH sa.shiftTemplate " +     // Template 조인 (시간 조회용)
            "WHERE r.requester.id = :requesterId " +
            "ORDER BY r.createdAt DESC")
    List<SubstituteRequest> findAllByRequesterIdOrderByCreatedAtDesc(@Param("requesterId") Long requesterId);

    // 본인을 제외한 같은 매장의 다른 직원들의 대타 요청 조회
    // Requester의 storeId가 일치하고(같은 매장 직원), requestId가 본인의 id와 다른 대타 요청
    @Query("SELECT r FROM SubstituteRequest r " +
            "JOIN FETCH r.requester m " +
            "JOIN FETCH m.user " +
            "JOIN FETCH r.shiftAssignment sa " +
            "JOIN FETCH sa.shiftTemplate " +
            "WHERE r.requester.store.id = :storeId " +
            "AND r.requester.id <> :requesterId " +
            "ORDER BY r.createdAt DESC")
    List<SubstituteRequest> findAllByRequester_Store_IdAndRequesterIdNotOrderByCreatedAtDesc(
            @Param("storeId") Long storeId,
            @Param("requesterId") Long requesterId);

    // 특정 매장의 모든 대타 요청 조회
    @Query("SELECT r FROM SubstituteRequest r " +
            "JOIN FETCH r.requester m " +
            "JOIN FETCH m.user " +
            "JOIN FETCH r.shiftAssignment sa " +
            "JOIN FETCH sa.shiftTemplate " +
            "WHERE r.requester.store.id = :storeId " +
            "ORDER BY r.createdAt DESC")
    List<SubstituteRequest> findAllByStoreId(@Param("storeId") Long storeId);
}
