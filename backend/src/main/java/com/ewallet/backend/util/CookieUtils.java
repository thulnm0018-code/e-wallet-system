package com.ewallet.backend.util;

import java.util.Objects;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtils {

    public static void createCookie(
            HttpServletResponse response,
            String name,
            String value,
            long maxAgeInSeconds) {

        ResponseCookie cookie = ResponseCookie.from(Objects.requireNonNull(name),Objects.requireNonNull(value))
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void clearCookie(HttpServletResponse response, String name) {

        ResponseCookie cookie = ResponseCookie.from(Objects.requireNonNull(name), "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}