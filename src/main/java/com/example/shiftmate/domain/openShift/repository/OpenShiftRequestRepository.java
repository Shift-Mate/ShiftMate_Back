package com.example.shiftmate.domain.openShift.repository;

import com.example.shiftmate.domain.openShift.entity.OpenShiftRequest;
import com.example.shiftmate.domain.openShift.status.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpenShiftRequestRepository extends JpaRepository<OpenShiftRequest, Long> {

    // 특정 매장의 모든 요청 조회
    @Query("SELECT o FROM OpenShiftRequest o " +
            "JOIN FETCH o.shiftTemplate " +
            "WHERE o.store.id = :storeId " +
            "ORDER BY o.workDate DESC")
    List<OpenShiftRequest> findAllByStoreIdOrderByWorkDateDesc(@Param("storeId") Long storeId);

    @Query("SELECT o FROM OpenShiftRequest o " +
            "JOIN FETCH o.shiftTemplate " +
            "WHERE o.store.id = :storeId " +
            "AND o.requestStatus IN :statuses " +
            "ORDER BY o.workDate DESC")
    List<OpenShiftRequest> findAllByStoreIdAndRequestStatusInOrderByWorkDateDesc(
            @Param("storeId") Long storeId,
            @Param("statuses") List<RequestStatus> statuses
    );

    // ID로 조회할 때 ShiftTemplate을 함께 가져오는 메서드
    @Query("SELECT o FROM OpenShiftRequest o JOIN FETCH o.shiftTemplate WHERE o.id = :id")
    Optional<OpenShiftRequest> findWithTemplateById(@Param("id") Long id);
}