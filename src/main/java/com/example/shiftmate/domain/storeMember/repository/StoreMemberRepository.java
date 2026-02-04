package com.example.shiftmate.domain.storeMember.repository;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreMemberRepository extends JpaRepository<StoreMember, Long> {
}
