package com.example.shiftmate.domain.store.repository;

import com.example.shiftmate.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // 사용자별 매장 목록 조회
    List<Store> findByUserId(Long userId);

    // 매장 id, 사용자 id로 조회 (권한 확인용)
    Optional<Store> findByIdAndUserId(Long id, Long userId);
}
