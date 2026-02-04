package com.example.shiftmate.global.security;

import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    // 토큰 종류 구분용 상수
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    // application.properties의 jwt 설정 주입
    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        // 시크릿 키로 서명 키 생성
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Access Token 생성
    public String createAccessToken(Long userId, String email) {
        return createToken(TOKEN_TYPE_ACCESS, userId, email, accessExpiration);
    }

    // Refresh Token 생성
    public String createRefreshToken(Long userId, String email) {
        return createToken(TOKEN_TYPE_REFRESH, userId, email, refreshExpiration);
    }

    // 공통 토큰 생성 로직
    private String createToken(String category, Long userId, String email, long exp) {
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId)) // 사용자 ID를 subject로 저장
                .claim("category", category)      // access/refresh 구분
                .claim("email", email)            // 이메일 저장
                .issuedAt(new Date())             // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + exp)) // 만료 시간
                .signWith(key);                   // 서명

        return builder.compact();
    }

    // 토큰 종류(access/refresh) 추출
    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
    }

    // 이메일 추출
    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // 사용자 ID 추출
    public Long getUserId(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    // 토큰 유효성 검사 (서명/만료 체크)
    public void validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (SecurityException e) {
            throw new CustomException(ErrorCode.INVALID_SIGNATURE);
        } catch (MalformedJwtException e) {
            throw new CustomException(ErrorCode.MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.EMPTY_TOKEN);
        }

    }

    // 공통 Claims 파싱
    private io.jsonwebtoken.Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}