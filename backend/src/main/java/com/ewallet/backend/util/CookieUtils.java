package com.ewallet.backend.util;

import java.util.Objects;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${cookie.secure:false}")
    private boolean secureCookie = false;

    @Value("${cookie.same-site:Lax}")
    private String sameSite = "Lax";

    public void createCookie(
            HttpServletResponse response,
            String name,
            String value,
            long maxAgeInSeconds) {

        ResponseCookie cookie = ResponseCookie.from(Objects.requireNonNull(name), Objects.requireNonNull(value))
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAgeInSeconds)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearCookie(HttpServletResponse response, String name) {

        ResponseCookie cookie = ResponseCookie.from(Objects.requireNonNull(name), "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}