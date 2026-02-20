package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubstituteRequestRepository extends JpaRepository<SubstituteRequest, Long>, JpaSpecificationExecutor<SubstituteRequest> {

    boolean existsByShiftAssignmentAndStatusIn(ShiftAssignment shiftAssignment, List<RequestStatus> statuses);

    @EntityGraph(attributePaths = {"requester.store", "requester.user", "shiftAssignment"})
    Optional<SubstituteRequest> findById(Long id);

    List<SubstituteRequest> findByShiftAssignmentInAndStatusIn(List<ShiftAssignment> assignments, Collection<RequestStatus> statuses);
}
