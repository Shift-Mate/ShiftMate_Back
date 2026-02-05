package com.example.shiftmate.domain.storeMember.repository;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import com.example.shiftmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreMemberRepository extends JpaRepository<StoreMember, Long> {

    Optional<StoreMember> findByStoreIdAndUserId(Long storeId, Long userId);
}
