package com.example.shiftmate.domain.user.repository;

import com.example.shiftmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    // 결과가 없을 수도 있는 조회라서 Optional
    Optional<User> findByEmail(String email);
}
