package com.ewallet.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig {

    private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173"
    );

    private CorsProperties corsProperties;

    public CorsConfig() {
        this(new CorsProperties());
    }

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    public void setCorsProperties(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        List<String> allowedOrigins = new ArrayList<>(corsProperties.getAllowedOrigins());
        if (allowedOrigins.isEmpty()) {
            allowedOrigins.addAll(DEFAULT_ALLOWED_ORIGINS);
        }

        config.setAllowedOrigins(allowedOrigins);

        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        config.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        config.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}