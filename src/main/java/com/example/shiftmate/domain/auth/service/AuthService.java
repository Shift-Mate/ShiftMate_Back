package com.example.shiftmate.domain.auth.service;

import com.example.shiftmate.domain.auth.dto.request.LoginRequest;
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

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

        // 4) 토큰을 응답 DTO로 반환
        return AuthResponse.from(accessToken, refreshToken);
    }
}
