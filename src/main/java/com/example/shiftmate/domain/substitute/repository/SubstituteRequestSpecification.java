package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.substitute.entity.SubstituteRequest;
import com.example.shiftmate.domain.substitute.status.RequestStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class SubstituteRequestSpecification {

    // Store ID 일치 (다른 직원 조회, 전체 조회용)
    public static Specification<SubstituteRequest> hasStoreId(Long storeId) {
        return (root, query, cb) -> cb.equal(root.get("requester").get("store").get("id"), storeId);
    }

    // Requester ID 일치 (내 요청 조회용)
    public static Specification<SubstituteRequest> hasRequesterId(Long requesterId) {
        return (root, query, cb) -> cb.equal(root.get("requester").get("id"), requesterId);
    }

    // Requester ID 불일치 (다른 직원 조회용)
    public static Specification<SubstituteRequest> notRequesterId(Long requesterId) {
        return (root, query, cb) -> cb.notEqual(root.get("requester").get("id"), requesterId);
    }

    // 상태 필터링 (null이면 전체 조회)
    public static Specification<SubstituteRequest> hasStatus(RequestStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    // N+1 문제 해결을 위한 Fetch Join
    public static Specification<SubstituteRequest> withFetch() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("requester", JoinType.LEFT).fetch("user", JoinType.LEFT);
                root.fetch("shiftAssignment", JoinType.LEFT).fetch("shiftTemplate", JoinType.LEFT);
            }
            return null;
        };
    }
}