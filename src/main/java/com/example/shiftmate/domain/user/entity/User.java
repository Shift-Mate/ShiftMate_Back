package com.example.shiftmate.domain.user.entity;

import com.example.shiftmate.global.common.entity.BaseCreateEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        // 소셜 계정 고유 식별 (provider + providerId)
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_provider_provider_id", columnNames = {"provider", "provider_id"})
        }
)
public class User extends BaseCreateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'LOCAL'")
    private AuthProvider provider; // LOCAL / KAKAO / GOOGLE

    @Column(name = "provider_id", length = 100)
    private String providerId;     // 소셜일 때 필수, LOCAL은 null 가능

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private boolean profileCompleted; // 소셜 가입 후 이름/전화번호 입력 완료 여부

    @Builder
    public User(String email, String name, String password, String phoneNumber,
                AuthProvider provider, String providerId, boolean profileCompleted) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.providerId = providerId;
        this.profileCompleted = profileCompleted;
    }

    // 비밀번호 변경 메서드
    // 서비스에서 새 비밀번호를 인코딩한 뒤 이 메서드로 반영
    public void changePassword(String encodedPassword) {
        // 인코딩된 비밀번호로 교체
        this.password = encodedPassword;
    }

    // 내 프로필(이름/전화번호) 수정 메서드
    public void updateProfile(String name, String phoneNumber) {
        // 이름이 null/빈값이 아니면 변경
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }

        // 전화번호가 null/빈값이 아니면 변경
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber.trim();
        }

        // 이름/전화번호가 모두 유효하게 채워지면 프로필 완료 상태로 전환
        this.profileCompleted = this.name != null && !this.name.isBlank()
                && this.phoneNumber != null && !this.phoneNumber.isBlank();
    }
}
