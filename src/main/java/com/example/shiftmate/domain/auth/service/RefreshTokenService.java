package com.example.shiftmate.domain.auth.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

// Refresh Token을 Redis에 저장/검증/삭제하는 서비스
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    // Redis 키 prefix: refresh:email 형태로 저장
    private static final String REFRESH_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    // refresh 토큰 만료시간(ms) - jwt 설정과 동일하게 맞춰 TTL로 사용
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // refresh 토큰 저장 (email 기준 1개만 유지)
    // 동일 이메일로 재로그인 시 기존 토큰은 덮어씀
    public void save(String email, String refreshToken) {
        String key = REFRESH_PREFIX + email;
        redisTemplate.opsForValue()
            .set(key, refreshToken, Duration.ofMillis(refreshExpiration));
    }

    // 저장된 refresh 토큰 조회
    public String get(String email) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + email);
    }

    // 저장된 refresh 토큰 삭제 (로그아웃/강제 만료 처리)
    public void delete(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);
    }

    // 전달된 refresh 토큰이 Redis에 저장된 값과 일치하는지 확인
    public boolean matches(String email, String refreshToken) {
        String saved = get(email);
        return saved != null && saved.equals(refreshToken);
    }
}
