package com.example.shiftmate.domain.auth.service;

import com.example.shiftmate.domain.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenCleanupScheduler {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    @Scheduled(cron = "0 */10 * * * *") // 10분마다 실행
    public void cleanupExpiredTokens() {
        // 만료된 토큰 일괄 삭제
        passwordResetTokenRepository.deleteExpiredTokens(Instant.now());
    }
}