package com.example.shiftmate.domain.storeMember.repository;

import com.example.shiftmate.domain.storeMember.entity.StoreMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreMemberRepository extends JpaRepository<StoreMember, Long>, JpaSpecificationExecutor<StoreMember> {
    
    // 같은 매장에 같은 사람이 등록되어있는지 (중복 체크) -> 삭제 되지 않은 것만
    @EntityGraph(attributePaths = {"store", "user"})
    @Query("SELECT sm FROM StoreMember sm WHERE sm.store.id = :storeId AND sm.user.id = :userId AND sm.deletedAt IS NULL")
    Optional<StoreMember> findByStoreIdAndUserId(@Param("storeId") Long storeId, @Param("userId") Long userId);

    // 전체 조회 (N+1 방지) -> 삭제 되지 않은 것만
    @EntityGraph(attributePaths = {"store", "user"})
    @Query("SELECT sm FROM StoreMember sm WHERE sm.deletedAt IS NULL")
    List<StoreMember> findAllWithRelations();

    @EntityGraph(attributePaths = {"store", "user"})
    @Query("SELECT sm FROM StoreMember sm WHERE sm.id = :id AND sm.deletedAt IS NULL")
    Optional<StoreMember> findByIdWithRelations(@Param("id") Long id);

    // 유저 기준 조회 (N+1 방지, @EntityGraph + JOIN FETCH 사용)
    // 유저 속한 매장 조회 -> 삭제 되지 않은 것만
    @EntityGraph(attributePaths = {"store"})
    @Query("SELECT sm FROM StoreMember sm WHERE sm.user.id = :userId AND sm.deletedAt IS NULL")
    List<StoreMember> findByUserId(@Param("userId") Long userId);

    // 가게 기준 조회 (N+1 방지, @EntityGraph + JOIN FETCH 사용)
    // 매장에 속한 멤버 조회 -> 삭제 되지 않은 것만
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT sm FROM StoreMember sm WHERE sm.store.id = :storeId AND sm.deletedAt IS NULL")
    List<StoreMember> findByStoreId(@Param("storeId") Long storeId);

    // 단건 조회 - 활성만 (soft delete 필터링)
    Optional<StoreMember> findByIdAndDeletedAtIsNull(Long id);

    // Specification과 함께 사용할 때 N+1 방지
    @EntityGraph(attributePaths = {"user"})
    List<StoreMember> findAll(Specification<StoreMember> spec);
}
