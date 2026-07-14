package com.ewallet.backend.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void shouldReturn429WhenLimitExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();

        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, filterChain);
        }

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(429);
    }
}
