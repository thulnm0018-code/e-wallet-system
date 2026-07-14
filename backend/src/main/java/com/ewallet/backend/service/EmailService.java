package com.ewallet.backend.service;

public interface EmailService {

    void sendOtpEmail(
            String email,
            String otpCode
    );
}