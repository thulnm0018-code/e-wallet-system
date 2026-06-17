package com.ewallet.backend.util;

public class PhoneUtils {

    public static String normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String raw = input.trim();
        String cleaned = raw.replaceAll("[^\\d]", "");

        if (cleaned.isEmpty()) {
            return null;
        }

        if (raw.startsWith("+")) {
            if (cleaned.startsWith("84") && cleaned.length() >= 3 && cleaned.charAt(2) == '0') {
                cleaned = "84" + cleaned.substring(3);
            }
            return "+" + cleaned;
        }

        if (cleaned.startsWith("0") && cleaned.length() >= 9) {
            return "+84" + cleaned.substring(1);
        }

        if (cleaned.startsWith("84")) {
            return "+" + cleaned;
        }

        return null;
    }
}