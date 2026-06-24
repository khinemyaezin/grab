package com.grab.store.identity.internal.api.rest.util;

import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieHelper {

    @Value("${security.api.cookie.secure:true}")
    private boolean secure;

    @Value("${security.api.cookie.same-site:Strict}")
    private String sameSite;

    @Value("${security.api.cookie.refresh-token-max-age-days:7}")
    private long refreshTokenMaxAgeDays;

    @Value("${security.api.cookie.refresh-token-path:/api/v1/identity/auth/refresh}")
    private String refreshTokenPath;

    public HttpHeaders createTokenCookies(AuthResponse response) {
        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", response.accessToken())
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(response.expiresInMs() / 1000)
                .sameSite(sameSite)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(secure)
                .path(refreshTokenPath)
                .maxAge(refreshTokenMaxAgeDays * 24 * 60 * 60)
                .sameSite(sameSite)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    public HttpHeaders clearTokenCookies() {
        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(secure).path("/").sameSite(sameSite).maxAge(0).build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(secure).path(refreshTokenPath).sameSite(sameSite).maxAge(0).build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }
}
