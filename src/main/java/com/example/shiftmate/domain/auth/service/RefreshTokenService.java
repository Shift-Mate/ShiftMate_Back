package com.example.shiftmate.domain.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

// Refresh Token을 Redis에 저장/검증/삭제하는 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    // Redis 키 prefix: refresh:email 형태로 저장
    private static final String REFRESH_PREFIX = "refresh:";
    private static final Map<String, FallbackToken> FALLBACK_STORE = new ConcurrentHashMap<>();

    private final StringRedisTemplate redisTemplate;

    // refresh 토큰 만료시간(ms) - jwt 설정과 동일하게 맞춰 TTL로 사용
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // Redis 장애 시 메모리 fallback 사용 여부 (개발 환경 대응)
    @Value("${auth.refresh.fallback-memory-enabled:true}")
    private boolean fallbackMemoryEnabled;

    // refresh 토큰 저장 (email 기준 1개만 유지)
    // 동일 이메일로 재로그인 시 기존 토큰은 덮어씀
    public void save(String email, String refreshToken) {
        String key = REFRESH_PREFIX + email;
        try {
            redisTemplate.opsForValue()
                .set(key, refreshToken, Duration.ofMillis(refreshExpiration));
            FALLBACK_STORE.remove(key);
        } catch (DataAccessException e) {
            if (!fallbackMemoryEnabled) {
                throw e;
            }
            log.warn("Redis unavailable. Fallback memory store is used for key={}", key);
            FALLBACK_STORE.put(key, new FallbackToken(
                refreshToken,
                Instant.now().plusMillis(refreshExpiration)
            ));
        }
    }

    // 저장된 refresh 토큰 조회
    public String get(String email) {
        String key = REFRESH_PREFIX + email;
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (DataAccessException e) {
            if (!fallbackMemoryEnabled) {
                throw e;
            }
            FallbackToken token = FALLBACK_STORE.get(key);
            if (token == null) {
                return null;
            }
            if (token.expiredAt().isBefore(Instant.now())) {
                FALLBACK_STORE.remove(key);
                return null;
            }
            return token.value();
        }
    }

    // 저장된 refresh 토큰 삭제 (로그아웃/강제 만료 처리)
    public void delete(String email) {
        String key = REFRESH_PREFIX + email;
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException e) {
            if (!fallbackMemoryEnabled) {
                throw e;
            }
            log.warn("Redis unavailable. Fallback memory delete is used for key={}", key);
        } finally {
            FALLBACK_STORE.remove(key);
        }
    }

    // 전달된 refresh 토큰이 Redis에 저장된 값과 일치하는지 확인
    public boolean matches(String email, String refreshToken) {
        String saved = get(email);
        return saved != null && saved.equals(refreshToken);
    }

    private record FallbackToken(String value, Instant expiredAt) {
    }
}
