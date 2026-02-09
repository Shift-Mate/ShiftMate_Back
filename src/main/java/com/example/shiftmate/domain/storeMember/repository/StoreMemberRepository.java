package com.example.shiftmate.domain.storeMember.repository;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreMemberRepository extends JpaRepository<StoreMember, Long> {

    // User ID로 StoreMember 조회
    Optional<StoreMember> findByUser_Id(Long userId);
    
    // Store ID와 User ID로 StoreMember 조회 (더 안전)
    Optional<StoreMember> findByStore_IdAndUser_Id(Long storeId, Long userId);
}
