package com.example.shiftmate.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetMailService {

    private final JavaMailSender mailSender;

    // 프론트 도메인 (예: http://localhost:3000)
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    // 발신자 주소 (보통 MAIL_USERNAME과 동일)
    @Value("${app.mail.from}")
    private String from;

    /**
     * 비밀번호 재설정 메일 발송
     * @param toEmail 수신자 이메일
     * @param token   재설정 토큰
     */
    public void sendResetMail(String toEmail, String token) {
        // 프론트 재설정 페이지 링크 생성
        String resetUrl = frontendBaseUrl + "/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("[ShiftMate] 비밀번호 재설정 안내");
        message.setText(
                "아래 링크를 눌러 비밀번호를 재설정하세요.\n\n"
                        + resetUrl
                        + "\n\n링크 유효시간: 30분"
        );

        mailSender.send(message);
    }
}