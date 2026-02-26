package com.example.shiftmate.domain.openShift.repository;

import com.example.shiftmate.domain.openShift.entity.OpenShiftApply;
import com.example.shiftmate.domain.openShift.status.ApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpenShiftApplyRepository extends JpaRepository<OpenShiftApply, Long> {

    boolean existsByRequestIdAndApplicantId(Long requestId, Long applicantId);

    // 승인된 지원자를 제외한 나머지 WAITING 상태의 지원들을 REJECTED로 변경
    @Modifying(clearAutomatically = true)
    @Query("UPDATE OpenShiftApply a SET a.applyStatus = :rejectedStatus " +
            "WHERE a.request.id = :requestId " +
            "AND a.applicant.id <> :applicantId " +
            "AND a.applyStatus = :waitingStatus")
    void rejectRemainingApplications(
            @Param("requestId") Long requestId,
            @Param("applicantId") Long applicantId,
            @Param("rejectedStatus") ApplyStatus rejectedStatus,
            @Param("waitingStatus") ApplyStatus waitingStatus
    );

    // 특정 오픈시프트의 지원자 목록 조회
    @Query("SELECT a FROM OpenShiftApply a " +
            "JOIN FETCH a.applicant m " +
            "JOIN FETCH m.user u " +
            "WHERE a.request.id = :requestId " +
            "ORDER BY a.createdAt ASC")
    List<OpenShiftApply> findAllByRequestId(@Param("requestId") Long requestId);

    // 나의 지원 내역 조회
    @Query("SELECT a FROM OpenShiftApply a " +
            "JOIN FETCH a.request r " +
            "JOIN FETCH r.shiftTemplate " +
            "JOIN FETCH a.applicant m " +
            "JOIN FETCH m.user u " +
            "WHERE m.id = :applicantId " +
            "ORDER BY a.createdAt DESC")
    List<OpenShiftApply> findAllByApplicantId(@Param("applicantId") Long applicantId);
}