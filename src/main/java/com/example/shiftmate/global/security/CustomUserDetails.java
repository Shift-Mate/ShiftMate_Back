package com.example.shiftmate.global.security;

import com.example.shiftmate.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Spring Security가 인증 정보를 다룰 때 사용하는 UserDetails 구현체
public class CustomUserDetails implements UserDetails {

    // 실제 사용자 엔티티를 감싸서 보관
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 편의 메서드: 사용자 PK
    // 다른곳에서 쉽게 꺼내 쓰게 하려고 추가한 것
    public Long getId() {
        return user.getId();
    }

    // 편의 메서드: 이메일
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재 role 사용 안 하므로 빈 권한 목록 반환
        return List.of();
    }

    @Override
    public String getPassword() {
        // Spring Security가 비밀번호 검증할 때 사용
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security에서 username으로 쓸 값 (여기서는 email)
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 만료 정책이 없으므로 항상 true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 잠금 정책 없으므로 true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 만료 정책 없으므로 true
    }

    @Override
    public boolean isEnabled() {
        return true; // 활성 사용자로 간주
    }
}