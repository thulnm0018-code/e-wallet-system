package com.ewallet.backend.security.filter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {

    return Bucket.builder()
            .addLimit(limit -> limit
                    .capacity(MAX_REQUESTS)
                    .refillGreedy(
                            MAX_REQUESTS,
                            Duration.ofMinutes(1)
                    )
            )
            .build();
}
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (isProtectedEndpoint(requestUri)) {

            String clientIp = getClientIp(request);

            Bucket bucket = cache.computeIfAbsent(
                    clientIp,
                    key -> createNewBucket()
            );

            if (!bucket.tryConsume(1)) {

                response.setStatus(
                        HttpStatus.TOO_MANY_REQUESTS.value()
                );

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"message\": \"Ban đang thao tac qua nhanh. Vui long thu lai sau 1 phut!\"}"
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedEndpoint(String requestUri) {
        return requestUri.contains("/api/v1/auth/login")
                || requestUri.contains("/api/v1/auth/forgot-password")
                || requestUri.contains("/api/v1/wallets/transfer");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}