package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubstituteRequestRepository extends JpaRepository<SubstituteRequest, Long> {

    boolean existsByShiftAssignmentAndStatusIn(ShiftAssignment shiftAssignment, List<RequestStatus> statuses);

}
