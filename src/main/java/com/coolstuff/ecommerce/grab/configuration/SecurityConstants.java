package com.coolstuff.ecommerce.grab.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConstants {
    public static final String TOKEN_COOKIE_KEY = "grab.auth.token";
    public static final String AUTH_HEADER_PREFIX = "Bearer ";
    private final String[] SKIP_URLS = new String[]{
            "/api/v1/public/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"
    };

    @Bean
    public String[] skipUrls() {
        return SKIP_URLS;
    }
}