package com.example.shiftmate.domain.employeePreference.repository;

import com.example.shiftmate.domain.employeePreference.entity.EmployeePreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeePreferenceRepository extends JpaRepository<EmployeePreference,Long> {


    boolean existsByMemberId(Long memberId);

    List<EmployeePreference> findByMemberId(Long memberId);
}
