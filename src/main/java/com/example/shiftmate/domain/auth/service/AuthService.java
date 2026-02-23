package com.example.shiftmate.domain.auth.service;

import com.example.shiftmate.domain.auth.dto.request.*;
import com.example.shiftmate.domain.auth.dto.response.AuthResponse;
import com.example.shiftmate.domain.auth.dto.response.SignUpResponse;
import com.example.shiftmate.domain.auth.dto.response.SocialUserInfo;
import com.example.shiftmate.domain.auth.entity.PasswordResetToken;
import com.example.shiftmate.domain.auth.entity.SignupEmailVerification;
import com.example.shiftmate.domain.auth.repository.PasswordResetTokenRepository;
import com.example.shiftmate.domain.auth.repository.SignupEmailVerificationRepository;
import com.example.shiftmate.domain.user.entity.AuthProvider;
import com.example.shiftmate.domain.user.entity.User;
import com.example.shiftmate.domain.user.repository.UserRepository;
import com.example.shiftmate.global.exception.CustomException;
import com.example.shiftmate.global.exception.ErrorCode;
import com.example.shiftmate.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthMailService authMailService;
    private final SignupEmailVerificationRepository signupEmailVerificationRepository;
    private final KakaoOauthService kakaoOauthService;
    private final GoogleOauthService googleOauthService;

    public SignUpResponse signUp(SignUpRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        // 회원가입 전 이메일 인증 완료 여부 확인
        SignupEmailVerification verification = signupEmailVerificationRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new CustomException(ErrorCode.SIGNUP_EMAIL_NOT_VERIFIED));

        if (!verification.isVerified() || verification.isExpired()) {
            throw new CustomException(ErrorCode.SIGNUP_EMAIL_NOT_VERIFIED);
        }

        // 1) 이메일 중복 체크: 동일 이메일로 중복 가입 방지
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 비밀번호와 비밀번호 확인 일치 검증
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }

        // 2) 사용자 엔티티 생성 (비밀번호는 평문 저장 금지 → BCrypt로 해시)
        User user = User.builder()
                .email(email)
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .provider(AuthProvider.LOCAL) // 로컬 계정 표시
                .providerId(null)
                .profileCompleted(true)
                .build();

        // 3) DB 저장 후 응답 DTO 반환
        User saved = userRepository.save(user);
        // 가입 완료 후 인증 기록 정리
        signupEmailVerificationRepository.deleteByEmail(email);
        return SignUpResponse.from(saved);

    }

    public AuthResponse socialLogin(AuthProvider provider, String code) {
        if (provider == AuthProvider.LOCAL) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        SocialUserInfo socialUser = switch (provider) {
            case KAKAO -> kakaoOauthService.getSocialUserInfo(code);
            case GOOGLE -> googleOauthService.getSocialUserInfo(code);
            default -> throw new CustomException(ErrorCode.INVALID_REQUEST);
        };

        String providerId = socialUser.getProviderId();
        String email = socialUser.getEmail();

        // 요구사항: 이메일 없으면 가입/로그인 불가
        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
            // 원하면 SOCIAL_EMAIL_REQUIRED 코드 새로 만들어서 더 명확히 처리 가능
        }

        // 1) provider+providerId 계정 있으면 그대로 로그인
        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElse(null);

        // 2) 없으면 "회원가입" 처리
        if (user == null) {
            // 일반 회원가입처럼 이메일 중복 막기
            if (userRepository.existsByEmail(email)) {
                throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }

            user = userRepository.save(User.builder()
                    .email(email)
                    .name((socialUser.getName() == null || socialUser.getName().isBlank()) ? "social_user" : socialUser.getName())
                    // 소셜 계정은 비밀번호 로그인 안 쓰므로 랜덤 저장
                    .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    // 현재 스키마 대응 기본값(추후 프로필 보완 유도 가능)
                    .phoneNumber("01000000000")
                    .provider(provider)
                    .providerId(providerId)
                    // 소셜 가입 직후에는 이름/전화번호 재입력을 강제
                    .profileCompleted(false)
                    .build());
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());
        refreshTokenService.save(user.getEmail(), refreshToken);

        return AuthResponse.from(accessToken, refreshToken);
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

    /**
     * 비밀번호 재설정 요청: 이메일로 토큰 발급 후 저장 (이메일 발송은 추후 연동)
     */
    public void requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim()).orElse(null);
        if (user == null) {
            return; // 보안상 해당 이메일이 없어도 동일 응답
        }

        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(30 * 60); // 30분

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .build();
        passwordResetTokenRepository.save(resetToken);

        // 토큰 저장 후, 사용자 이메일로 재설정 링크 발송
        try {
            Locale locale = LocaleContextHolder.getLocale();
            // 메일 발송 실패하더라도 API 응답은 동일하게 유지(보안상)
            authMailService.sendResetMail(user.getEmail(), token, locale);
        } catch (Exception e) {
            // 운영 추적용 로그 (사용자에게는 에러 노출 X)
            log.warn("Password reset mail send failed. email={}", request.getEmail(), e);
        }
    }

    /**
     * 토큰 + 새 비밀번호로 비밀번호 재설정
     */
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.NEW_PASSWORD_CONFIRM_MISMATCH);
        }

        User user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 비밀번호가 바뀌었으므로 기존 refresh 토큰을 삭제해서
        // 다른 기기/기존 로그인 세션이 자동으로 끊기도록 처리
        refreshTokenService.delete(user.getEmail());

        // 사용한 재설정 토큰은 1회성이라 즉시 삭제
        passwordResetTokenRepository.delete(resetToken);
    }

    public void requestSignupEmailVerification(SignupEmailVerificationRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 이미 가입된 이메일이면 인증코드 발송하지 않음
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 기존 코드 정리 후 새 코드 발급
        signupEmailVerificationRepository.deleteByEmail(email);

        // 6자리 숫자 코드 생성 (100000 ~ 999999)
        int raw = 100000 + new java.util.Random().nextInt(900000);
        String code = String.valueOf(raw);

        SignupEmailVerification verification = SignupEmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(Instant.now().plusSeconds(10 * 60)) // 10분
                .verified(false)
                .build();

        signupEmailVerificationRepository.save(verification);

        try {
            Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale();
            authMailService.sendSignupVerificationCodeMail(email, code, locale);
        } catch (Exception e) {
            // 메일 발송 실패 시 API도 실패로 응답해 프론트에서 즉시 인지 가능하게 한다.
            log.warn("Signup verify mail send failed. email={}", email, e);
            throw new CustomException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    public void confirmSignupEmailVerification(SignupEmailVerificationConfirmRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode().trim();

        SignupEmailVerification verification = signupEmailVerificationRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new CustomException(ErrorCode.SIGNUP_VERIFICATION_NOT_FOUND));

        if (verification.isExpired()) {
            throw new CustomException(ErrorCode.SIGNUP_VERIFICATION_EXPIRED);
        }

        if (!verification.getCode().equals(code)) {
            throw new CustomException(ErrorCode.SIGNUP_VERIFICATION_CODE_MISMATCH);
        }

        // 코드 일치 + 만료 아님 => 인증 완료 처리
        verification.markVerified();
    }
    public AuthResponse kakaoLogin(String code) {
        // 구 엔드포인트 호환용: 공통 소셜 로직으로 위임
        return socialLogin(AuthProvider.KAKAO, code);
    }
}
