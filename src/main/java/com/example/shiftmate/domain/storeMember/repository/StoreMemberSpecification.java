package com.example.shiftmate.domain.storeMember.repository;

import com.example.shiftmate.domain.storeMember.entity.Department;
import com.example.shiftmate.domain.storeMember.entity.MemberStatus;
import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.storeMember.entity.StoreRole;
import org.springframework.data.jpa.domain.Specification;

public class StoreMemberSpecification {

    public static Specification<StoreMember> hasStoreId(Long storeId) {
        return (root, query, cb) -> 
            storeId == null ? null : cb.equal(root.get("store").get("id"), storeId);
    }

    public static Specification<StoreMember> hasStatus(MemberStatus status) {
        return (root, query, cb) -> 
            status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<StoreMember> hasRole(StoreRole role) {
        return (root, query, cb) -> 
            role == null ? null : cb.equal(root.get("role"), role);
    }

    public static Specification<StoreMember> hasDepartment(Department department) {
        return (root, query, cb) -> 
            department == null ? null : cb.equal(root.get("department"), department);
    }

    public static Specification<StoreMember> hasUserId(Long userId) {
        return (root, query, cb) -> 
            userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }
}
