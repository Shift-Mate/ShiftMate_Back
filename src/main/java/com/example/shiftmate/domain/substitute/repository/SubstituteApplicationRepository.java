package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.substitute.dto.response.SubstituteApplicationResDto;
import com.example.shiftmate.domain.substitute.entity.SubstituteApplication;
import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubstituteApplicationRepository extends JpaRepository<SubstituteApplication, Long> {

    boolean existsByRequestIdAndApplicantId(Long requestId, Long applicantId);

    // 내 지원 내역 조회
    @Query("SELECT sa FROM SubstituteApplication sa " +
            "JOIN FETCH sa.request r " +             // 대타 요청
            "JOIN FETCH r.shiftAssignment s " +      // 스케줄
            "JOIN FETCH s.shiftTemplate " +          // 시간 정보 조회를 위한 템플릿
            "JOIN FETCH r.requester req " +          // 요청자
            "JOIN FETCH req.user " +                 // 요청자 이름
            "JOIN FETCH sa.applicant app " +         // 지원자
            "JOIN FETCH app.user " +                 // 지원자 이름
            "WHERE sa.applicant.id = :applicantId " +
            "ORDER BY sa.createdAt DESC")
    List<SubstituteApplication> findAllByApplicantId(@Param("applicantId") Long applicantId);

    boolean existsByRequestIdAndStatus(Long requestId, ApplicationStatus status);

    // 특정 대타 요청에 대한 모든 지원 내역 조회
    @Query("SELECT sa FROM SubstituteApplication sa " +
            "JOIN FETCH sa.applicant app " +
            "JOIN FETCH app.user " +
            "WHERE sa.request.id = :requestId " +
            "ORDER BY sa.createdAt ASC")
    List<SubstituteApplication> findAllByRequestId(@Param("requestId") Long requestId);
}
