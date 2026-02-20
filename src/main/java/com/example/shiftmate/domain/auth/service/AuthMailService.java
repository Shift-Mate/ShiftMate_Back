package com.example.shiftmate.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthMailService {

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;

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
    public void sendResetMail(String toEmail, String token, Locale locale) {
        // 프론트 재설정 페이지 링크 생성
        String resetUrl = frontendBaseUrl + "/auth/reset-password?token=" + token;
        String subject = messageSource.getMessage("mail.reset.subject", null, locale);
        String bodyIntro = messageSource.getMessage("mail.reset.body.intro", null, locale);
        String bodyExpire = messageSource.getMessage("mail.reset.body.expire", null, locale);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(
                bodyIntro + "\n\n"
                        + resetUrl
                        + "\n\n" + bodyExpire
        );

        mailSender.send(message);
    }

    public void sendSignupVerificationCodeMail(String toEmail, String code, Locale locale) {
        // locale 별 메시지 로드
        String subject = messageSource.getMessage("mail.signup.verify.subject", null, locale);
        String intro = messageSource.getMessage("mail.signup.verify.body.intro", null, locale);
        String expire = messageSource.getMessage("mail.signup.verify.body.expire", null, locale);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject(subject);

        // 코드가 눈에 띄게 줄바꿈 구성
        message.setText(
                intro + "\n\n" +
                        "Code: " + code + "\n\n" +
                        expire
        );

        mailSender.send(message);
    }
}