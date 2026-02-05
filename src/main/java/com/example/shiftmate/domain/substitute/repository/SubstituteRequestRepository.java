package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.shiftAssignment.entity.ShiftAssignment;
import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubstituteRequestRepository extends JpaRepository<SubstituteRequest, Long> {

    boolean existsByShiftAssignmentAndStatusIn(ShiftAssignment shiftAssignment, List<RequestStatus> statuses);

    // 본인의 대타 요청 조회
    List<SubstituteRequest> findAllByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    // 본인을 제외한 같은 매장의 다른 직원들의 대타 요청 조회
    // Requester의 storeId가 일치하고(같은 매장 직원), requestId가 본인의 id와 다른 대타 요청
    List<SubstituteRequest> findAllByRequester_Store_IdAndRequesterIdNotOrderByCreatedAtDesc(Long storeId, Long requesterId);
}
