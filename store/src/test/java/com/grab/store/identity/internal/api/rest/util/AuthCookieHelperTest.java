package com.grab.store.identity.internal.api.rest.util;

import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthCookieHelperTest {

    private AuthCookieHelper authCookieHelper;

    @BeforeEach
    void setUp() {
        authCookieHelper = new AuthCookieHelper();
        ReflectionTestUtils.setField(authCookieHelper, "secure", true);
        ReflectionTestUtils.setField(authCookieHelper, "sameSite", "Strict");
        ReflectionTestUtils.setField(authCookieHelper, "refreshTokenMaxAgeDays", 7L);
        ReflectionTestUtils.setField(authCookieHelper, "refreshTokenPath", "/api/v1/identity/auth/refresh");
    }

    @Test
    void shouldCreateTokenCookies() {
        AuthResponse response = new AuthResponse(
                "access-token-123",
                "refresh-token-456",
                3600000L, // 1 hour
                "user-1",
                "test@test.com",
                Set.of("CUSTOMER"),
                "ACTIVE"
        );

        HttpHeaders headers = authCookieHelper.createTokenCookies(response);
        
        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        assertThat(cookies).hasSize(2);

        String jwtCookie = cookies.stream().filter(c -> c.startsWith("accessToken=")).findFirst().orElseThrow();
        assertThat(jwtCookie).contains("accessToken=access-token-123");
        assertThat(jwtCookie).contains("HttpOnly");
        assertThat(jwtCookie).contains("Secure");
        assertThat(jwtCookie).contains("Path=/");
        assertThat(jwtCookie).contains("Max-Age=3600");
        assertThat(jwtCookie).contains("SameSite=Strict");

        String refreshCookie = cookies.stream().filter(c -> c.startsWith("refreshToken=")).findFirst().orElseThrow();
        assertThat(refreshCookie).contains("refreshToken=refresh-token-456");
        assertThat(refreshCookie).contains("HttpOnly");
        assertThat(refreshCookie).contains("Secure");
        assertThat(refreshCookie).contains("Path=/api/v1/identity/auth/refresh");
        assertThat(refreshCookie).contains("Max-Age=604800"); // 7 days in seconds
        assertThat(refreshCookie).contains("SameSite=Strict");
    }

    @Test
    void shouldClearTokenCookies() {
        HttpHeaders headers = authCookieHelper.clearTokenCookies();

        List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        assertThat(cookies).hasSize(2);

        String jwtCookie = cookies.stream().filter(c -> c.startsWith("accessToken=")).findFirst().orElseThrow();
        assertThat(jwtCookie).contains("accessToken=");
        assertThat(jwtCookie).contains("Max-Age=0");

        String refreshCookie = cookies.stream().filter(c -> c.startsWith("refreshToken=")).findFirst().orElseThrow();
        assertThat(refreshCookie).contains("refreshToken=");
        assertThat(refreshCookie).contains("Max-Age=0");
    }
}
