package com.example.shiftmate.domain.employeePreference.repository;

import com.example.shiftmate.domain.employeePreference.entity.EmployeePreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface employeePreferenceRepository extends JpaRepository<EmployeePreference,Long> {


    boolean existsByMemberId(Long memberId);
}
