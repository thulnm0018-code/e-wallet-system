package com.ewallet.backend.service;

import com.ewallet.backend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpDeliveryService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String activeProfile;
    private final boolean deliveryRequired;

    public OtpDeliveryService(ObjectProvider<JavaMailSender> mailSenderProvider,
                              @Value("${app.mail.from:}") String from,
                              @Value("${spring.profiles.active:}") String activeProfile,
                              @Value("${app.mail.required:true}") boolean deliveryRequired) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.activeProfile = activeProfile;
        this.deliveryRequired = deliveryRequired;
    }

    public void send(String recipient, String otp, String purpose) {
        if (!deliveryRequired) {
            return;
        }
        if (from == null || from.isBlank()) {
            if (isTestEnvironment()) {
                return;
            }
            throw new BusinessException("OTP email delivery is not configured: set MAIL_FROM");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            if (isTestEnvironment()) {
                return;
            }
            throw new BusinessException("OTP email delivery is not configured: check spring-boot-starter-mail and SMTP settings");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("E-Wallet verification code");
        message.setText("Your " + purpose + " verification code is " + otp
                + ". It expires in 5 minutes. Do not share this code.");
        mailSender.send(message);
    }

    private boolean isTestEnvironment() {
        return "test".equals(activeProfile);
    }
}