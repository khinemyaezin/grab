package com.grab.app.platform.seller.internal.adapter.inbound.rest.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SellerSecurityProperties {

    @Value("${security.local-jwt.issuer:${JWT_ISSUER:https://identity.local.grab}}")
    private String issuer;

    @Value("${security.local-jwt.audience:${JWT_AUDIENCE:grab-commerce-api}}")
    private String audience;

    @Value("${security.api.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.api.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${security.api.cookie.refresh-token-max-age-days:7}")
    private long refreshTokenMaxAgeDays;

    @Value("${security.api.cookie.refresh-token-path:/api/v1/identity/auth/refresh}")
    private String refreshTokenPath;

    @Value("${security.seller.cookie.access-token-path:/}")
    private String accessTokenPath;

    public String getIssuer() {
        return issuer;
    }

    public String getAudience() {
        return audience;
    }

    public boolean isCookieSecure() {
        return cookieSecure;
    }

    public String getCookieSameSite() {
        return cookieSameSite;
    }

    public long getRefreshTokenMaxAgeDays() {
        return refreshTokenMaxAgeDays;
    }

    public String getRefreshTokenPath() {
        return refreshTokenPath;
    }

    public String getAccessTokenPath() {
        return accessTokenPath;
    }
}

