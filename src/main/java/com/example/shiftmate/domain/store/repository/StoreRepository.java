package com.example.shiftmate.domain.store.repository;

import com.example.shiftmate.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // userId별 활성 매장 목록 (매장 선택 시 사용)
    List<Store> findByUserIdAndDeletedAtIsNull(Long userId);

    // userId별 전체 매장 목록 (삭제 포함, 마이페이지용)
    @Query(value = "SELECT * FROM stores WHERE user_id = :userId", nativeQuery = true)
    List<Store> findByUserIdIncludingDeleted(@Param("userId") Long userId);

    // 단건 조회 - 활성만
    Optional<Store> findByIdAndDeletedAtIsNull(Long id);

    // 단건 조회 - 삭제 포함
    @Query(value = "SELECT * FROM stores WHERE id = :id", nativeQuery = true)
    Optional<Store> findByIdIncludingDeleted(@Param("id") Long id);

    // 매장 id, 사용자 id로 조회 (권한 확인용) - 활성만
    Optional<Store> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    // 전체 매장 목록 조회 (활성만) - 테스트용
    List<Store> findAllByDeletedAtIsNull();
}
