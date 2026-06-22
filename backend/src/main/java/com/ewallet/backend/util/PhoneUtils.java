package com.ewallet.backend.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneUtils {

    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public static String normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        try {
            String raw = input.trim();
            PhoneNumber number = phoneUtil.parse(raw, "VN");

            if (phoneUtil.isValidNumber(number)) {
                return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (Exception e) {
            return null; 
        }

        return null;
    }
}