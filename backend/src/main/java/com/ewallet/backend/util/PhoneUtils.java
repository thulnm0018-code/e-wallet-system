package com.ewallet.backend.util;

public class PhoneUtils {

    public static String normalize(String phone) {
        if (phone == null) return null;
        
        String cleanPhone = phone.replaceAll("[^\\d]", "");

        if (cleanPhone.startsWith("84")) {
            cleanPhone = "0" + cleanPhone.substring(2);
        }

        return cleanPhone;
    }
}