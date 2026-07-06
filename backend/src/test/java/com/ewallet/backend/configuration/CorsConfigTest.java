package com.ewallet.backend.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {

    @Test
    void shouldExposeClientUrlsAsAllowedOriginsWhenNoExplicitOriginsAreConfigured() {
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setUrl("http://localhost:3000");
        corsProperties.setDevUrl("http://localhost:5173");

        CorsConfig corsConfig = new CorsConfig();

        CorsConfigurationSource source = corsConfig.corsConfigurationSource();

        CorsConfiguration configuration = ((UrlBasedCorsConfigurationSource) source)
                .getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(configuration);
        assertThat(configuration.getAllowedOrigins())
                .containsExactlyInAnyOrder("http://localhost:3000", "http://localhost:5173");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
