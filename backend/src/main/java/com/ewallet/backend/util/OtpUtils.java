package com.ewallet.backend.util;

import java.util.Random;

public class OtpUtils {

    private static final Random RANDOM = new Random();

    public static String generateOtp() {
        return String.format("%06d",
                RANDOM.nextInt(1000000));
    }
}