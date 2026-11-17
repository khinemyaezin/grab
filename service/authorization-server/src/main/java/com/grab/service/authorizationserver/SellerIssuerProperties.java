package com.grab.service.authorizationserver;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties("grab.seller-issuer")
record SellerIssuerProperties(String issuer, String clientId, String clientSecretHash, String redirectUri,
                              String postLogoutRedirectUri, String audience, Duration accessTokenTtl,
                              Duration refreshTokenTtl, String accountHost, int accountPort, Path keyStore,
                              String keyStorePassword, String signingAlias, List<String> retiringAliases,
                              Path accountClientKeyStore, String accountClientKeyPassword,
                              Path accountTrustStore, String accountTrustPassword) {
    @Override
    public String toString() {
        return "SellerIssuerProperties[redacted]";
    }
}
