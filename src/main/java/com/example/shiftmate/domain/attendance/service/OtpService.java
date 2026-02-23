package com.example.shiftmate.domain.attendance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String OTP_PREFIX = "otp:user:";
    private static final long OTP_VALID_TIME_SEC = 60L; // 1분으로 설정

    // OTP 발급 및 저장
    // OTP 번호는 마이페이지에서 발급받으므로 userId 사용
    public String generateAndSaveOtp(Long userId) {
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000)); // 6자리 난수 생성
        String key = OTP_PREFIX + userId;

        // Redis에 1분 설정하여 저장
        stringRedisTemplate.opsForValue().set(key, otp, Duration.ofSeconds(OTP_VALID_TIME_SEC));

        return otp;
    }

    // OTP 검증
    public boolean validateOtp(Long userId, String otp) {
        String key = OTP_PREFIX + userId;
        String savedOtp = stringRedisTemplate.opsForValue().get(key);

        if(savedOtp != null && savedOtp.equals(otp)) {
            stringRedisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
