package com.ewallet.backend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "client")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();
    private String url;
    private String devUrl;

    public List<String> getAllowedOrigins() {
        Set<String> origins = new LinkedHashSet<>();

        if (allowedOrigins != null) {
            allowedOrigins.stream()
                    .filter(StringUtils::hasText)
                    .forEach(origins::add);
        }

        if (StringUtils.hasText(url)) {
            origins.add(url);
        }

        if (StringUtils.hasText(devUrl)) {
            origins.add(devUrl);
        }

        return new ArrayList<>(origins);
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDevUrl() {
        return devUrl;
    }

    public void setDevUrl(String devUrl) {
        this.devUrl = devUrl;
    }
}