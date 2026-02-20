package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.substitute.entity.SubstituteApplication;
import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubstituteApplicationRepository extends JpaRepository<SubstituteApplication, Long>, JpaSpecificationExecutor<SubstituteApplication> {

    boolean existsByRequestIdAndApplicantId(Long requestId, Long applicantId);

    boolean existsByRequestIdAndStatusAndIdNot(Long id, ApplicationStatus applicationStatus, Long applicationId);

    @Modifying
    @Query("UPDATE SubstituteApplication sa SET sa.status = :newStatus WHERE sa.request.id = :requestId AND sa.status = :oldStatus")
    void updateStatusByRequestIdAndStatus(
            @Param("requestId") Long requestId,
            @Param("oldStatus") ApplicationStatus oldStatus,
            @Param("newStatus") ApplicationStatus newStatus);

    @Modifying
    @Query("UPDATE SubstituteApplication sa SET sa.status = 'REJECTED' WHERE sa.request.id = :requestId AND sa.status = 'WAITING' AND sa.id != :selectedId")
    void rejectRemainingApplications(@Param("requestId") Long requestId, @Param("selectedId") Long selectedId);
}
