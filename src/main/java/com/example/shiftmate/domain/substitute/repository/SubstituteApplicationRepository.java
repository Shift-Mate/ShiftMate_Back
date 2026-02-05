package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.substitute.entity.SubstituteApplication;
import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubstituteApplicationRepository extends JpaRepository<SubstituteApplication, Long> {

    boolean existsByRequestIdAndApplicantId(Long requestId, Long id);

}
