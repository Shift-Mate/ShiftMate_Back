package com.example.shiftmate.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "signup_email_verifications")
public class SignupEmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 인증 대상 이메일
    @Column(nullable = false)
    private String email;

    // 6자리 인증코드
    @Column(nullable = false, length = 6)
    private String code;

    // 코드 만료 시각
    @Column(nullable = false)
    private Instant expiresAt;

    // 인증 완료 여부
    @Column(nullable = false)
    private boolean verified;

    // 인증 완료 시각 (선택)
    private Instant verifiedAt;

    @Builder
    public SignupEmailVerification(String email, String code, Instant expiresAt, boolean verified) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.verified = verified;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void markVerified() {
        this.verified = true;
        this.verifiedAt = Instant.now();
    }
}