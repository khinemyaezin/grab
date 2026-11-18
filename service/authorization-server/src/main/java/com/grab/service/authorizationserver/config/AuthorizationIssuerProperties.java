package com.grab.service.authorizationserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties("grab.authorization-server")
public record AuthorizationIssuerProperties(
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String accountHost,
        int accountPort,
        Path keyStore,
        String keyStorePassword,
        String signingAlias,
        List<String> retiringAliases,
        Path accountClientKeyStore,
        String accountClientKeyPassword,
        Path accountTrustStore,
        String accountTrustPassword,
        @DefaultValue("1500") int accountConnectTimeout,
        @DefaultValue("3000") int accountReadTimeout,
        @DefaultValue("30") int rateLimitMaxAttempts,
        @DefaultValue("60") int rateLimitWindowSeconds,
        @DefaultValue("P1D") Duration tokenRetention,
        @DefaultValue("6021278") long clientInitAdvisoryLock
) {
    @Override
    public String toString() {
        return "AuthorizationIssuerProperties[redacted]";
    }
}
