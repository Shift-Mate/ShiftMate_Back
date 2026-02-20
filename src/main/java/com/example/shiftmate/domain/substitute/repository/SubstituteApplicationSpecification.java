package com.example.shiftmate.domain.substitute.repository;

import com.example.shiftmate.domain.substitute.entity.SubstituteApplication;
import com.example.shiftmate.domain.substitute.status.ApplicationStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class SubstituteApplicationSpecification {

    // Applicant ID 일치 (내 지원 내역 조회)
    public static Specification<SubstituteApplication> hasApplicantId(Long applicantId) {
        return (root, query, cb) -> cb.equal(root.get("applicant").get("id"), applicantId);
    }

    // Request ID 일치 (특정 요청의 지원자 목록 조회)
    public static Specification<SubstituteApplication> hasRequestId(Long requestId) {
        return (root, query, cb) -> cb.equal(root.get("request").get("id"), requestId);
    }

    // 상태 필터링
    public static Specification<SubstituteApplication> hasStatus(ApplicationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    // N+1 방지 Fetch Join
    public static Specification<SubstituteApplication> withFetch() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("applicant", JoinType.LEFT).fetch("user", JoinType.LEFT);
                root.fetch("request", JoinType.LEFT).fetch("requester", JoinType.LEFT).fetch("user", JoinType.LEFT);
                root.fetch("request", JoinType.LEFT).fetch("shiftAssignment", JoinType.LEFT).fetch("shiftTemplate", JoinType.LEFT);
            }
            return null;
        };
    }
}