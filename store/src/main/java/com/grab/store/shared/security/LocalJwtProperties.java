package com.grab.store.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("security.local-jwt")
public record LocalJwtProperties(
        String issuer,
        String audience,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {
    public LocalJwtProperties {
        if (issuer == null || issuer.isBlank())
            throw new IllegalArgumentException("JWT issuer is required");

        if (audience == null || audience.isBlank())
            throw new IllegalArgumentException("JWT audience is required");

        if (accessTokenTtl == null || accessTokenTtl.isNegative() || accessTokenTtl.isZero())
            throw new IllegalArgumentException("access token TTL must be positive");

        if (refreshTokenTtl == null || refreshTokenTtl.isNegative() || refreshTokenTtl.isZero())
            throw new IllegalArgumentException("refresh token TTL must be positive");
    }
}
