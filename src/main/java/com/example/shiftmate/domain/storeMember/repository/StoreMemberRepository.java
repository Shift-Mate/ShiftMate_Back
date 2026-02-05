package com.example.shiftmate.domain.storeMember.repository;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreMemberRepository extends JpaRepository<StoreMember, Long> {
    // 같은 매장에 같은 사람이 등록되어있는지(중복 체크)
    Optional<StoreMember> findByStoreIdAndUserId(Long storeId, Long userId);
}
