package com.ewallet.backend.service;

import com.ewallet.backend.service.impl.EmailServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @SuppressWarnings("null")
    @Test
    void shouldSendOtpEmail() {

        emailService.sendOtpEmail(
                "user@test.com",
                "123456"
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }
}