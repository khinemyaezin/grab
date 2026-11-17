package com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties("grab.seller-oidc")
public record SellerOidcProperties(boolean enabled, String issuer, String backchannel, String audience, String clientId,
                                   String clientSecret, String callback, URI ui, URI postLogout,
                                   String accountHost, int registrationPort, boolean secureCookies,
                                   boolean legacyBearer) {
    String endpoint(String path) {
        String base = backchannel == null || backchannel.isBlank() ? issuer : backchannel;
        return base + path;
    }

    @Override
    public String toString() {
        return "SellerOidcProperties[redacted]";
    }
}
