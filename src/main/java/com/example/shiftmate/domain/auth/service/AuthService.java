package com.example.shiftmate.domain.auth.service;

import com.example.shiftmate.domain.auth.dto.request.LoginRequest;
import com.example.shiftmate.domain.auth.dto.request.LogoutRequest;
import com.example.shiftmate.domain.auth.dto.request.SignUpRequest;
import com.example.shiftmate.domain.auth.dto.response.AuthResponse;
import com.example.shiftmate.domain.auth.dto.response.SignUpResponse;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import com.example.shiftmate.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.shiftmate.domain.auth.dto.request.RefreshRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public SignUpResponse signUp(SignUpRequest request) {
        // 1) 이메일 중복 체크: 동일 이메일로 중복 가입 방지
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2) 사용자 엔티티 생성 (비밀번호는 평문 저장 금지 → BCrypt로 해시)
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // 3) DB 저장 후 응답 DTO 반환
        User saved = userRepository.save(user);
        return SignUpResponse.from(saved);
    }

    public AuthResponse login(LoginRequest request) {
        // 1) 이메일로 사용자 조회 (없으면 404 처리)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 2) 비밀번호 검증 (해시 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 3) Access/Refresh 토큰 생성
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());

        // 4) refresh 토큰을 Redis에 저장 (로그아웃/재발급 검증용)
        refreshTokenService.save(user.getEmail(), refreshToken);

        // 5) 토큰을 응답 DTO로 반환
        return AuthResponse.from(accessToken, refreshToken);
    }

    public AuthResponse reissue(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1) 토큰 자체 유효성 검사 + Claims 한번만 파싱
        io.jsonwebtoken.Claims claims = jwtProvider.parseClaims(refreshToken);

        // 2) refresh 토큰인지 확인
        if (!"refresh".equals(claims.get("category", String.class))) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 3) 토큰에서 이메일 추출
        String email = claims.get("email", String.class);
        if (email == null) {
            throw new CustomException(ErrorCode.MALFORMED_TOKEN);
        }

        // 4) Redis에 저장된 토큰과 일치 확인
        if (!refreshTokenService.matches(email, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 5) 새 토큰 발급
        Long userId = Long.valueOf(claims.getSubject());
        String newAccess = jwtProvider.createAccessToken(userId, email);
        String newRefresh = jwtProvider.createRefreshToken(userId, email);

        // 6) Redis 갱신
        refreshTokenService.save(email, newRefresh);

        // 7) 응답 반환
        return AuthResponse.from(newAccess, newRefresh);
    }

    public void logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1) 토큰 유효성 확인 + Claims 한번만 파싱
        io.jsonwebtoken.Claims claims = jwtProvider.parseClaims(refreshToken);

        // 2) refresh 토큰인지 확인
        if (!"refresh".equals(claims.get("category", String.class))) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 3) 이메일 추출 후 Redis에서 삭제
        String email = claims.get("email", String.class);
        if (email == null) {
            throw new CustomException(ErrorCode.MALFORMED_TOKEN);
        }
        refreshTokenService.delete(email);
    }
}
