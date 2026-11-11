package com.grab.app.platform.seller.internal.adapter.inbound.rest.security.util;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.SellerSecurityProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SellerAuthCookieHelper {

    private final SellerSecurityProperties properties;

    public SellerAuthCookieHelper(SellerSecurityProperties properties) {
        this.properties = properties;
    }

    public HttpHeaders createTokenCookies(String accessToken, String refreshToken, long expiresInMs) {
        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .path(properties.getAccessTokenPath())
                .maxAge(expiresInMs / 1000)
                .sameSite(properties.getCookieSameSite())
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .path(properties.getRefreshTokenPath())
                .maxAge(properties.getRefreshTokenMaxAgeDays() * 24 * 60 * 60)
                .sameSite(properties.getCookieSameSite())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    public HttpHeaders clearTokenCookies() {
        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .path(properties.getAccessTokenPath())
                .sameSite(properties.getCookieSameSite())
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .path(properties.getRefreshTokenPath())
                .sameSite(properties.getCookieSameSite())
                .maxAge(0)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }
}

