package com.example.shiftmate.domain.storeMember.repository;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreMemberRepository extends JpaRepository<StoreMember, Long> {
    // 같은 매장에 같은 사람이 등록되어있는지(중복 체크)
    @Query("SELECT sm FROM StoreMember sm WHERE sm.store.id = :storeId AND sm.user.id = :userId")
    Optional<StoreMember> findByStoreIdAndUserId(@Param("storeId") Long storeId, @Param("userId") Long userId);
}
