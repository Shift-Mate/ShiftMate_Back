package com.example.shiftmate.domain.user.entity;

import com.example.shiftmate.global.common.entity.BaseCreateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
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

    @Builder
    public User(String email, String name, String password, String phoneNumber) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
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
    }
}
