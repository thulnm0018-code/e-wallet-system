package com.ewallet.backend.service.impl;

import com.ewallet.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl
        implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendOtpEmail(
            String email,
            String otpCode
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(email);

        message.setSubject(
                "E-Wallet OTP Verification"
        );

        message.setText(
                """
                Hello,

                Your OTP code is: %s

                This OTP expires in 5 minutes.

                Do not share this OTP with anyone.

                E-Wallet Team
                """
                        .formatted(otpCode)
        );

        mailSender.send(message);
    }
}