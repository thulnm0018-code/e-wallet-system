package com.ewallet.backend.util;

import java.security.SecureRandom;

public class OtpUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateOtp() {
        return String.format("%06d",
                RANDOM.nextInt(1000000));
    }
}