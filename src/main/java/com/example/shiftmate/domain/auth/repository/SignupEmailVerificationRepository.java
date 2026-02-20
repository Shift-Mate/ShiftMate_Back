package com.example.shiftmate.domain.auth.repository;

import com.example.shiftmate.domain.auth.entity.SignupEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignupEmailVerificationRepository extends JpaRepository<SignupEmailVerification, Long> {

    // 가장 최근 요청 1건 조회
    Optional<SignupEmailVerification> findTopByEmailOrderByIdDesc(String email);

    // 새 코드 발급 전에 기존 기록 정리
    void deleteByEmail(String email);
}