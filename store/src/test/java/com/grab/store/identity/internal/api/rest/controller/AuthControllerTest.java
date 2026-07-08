package com.grab.store.identity.internal.api.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grab.store.identity.internal.api.rest.assembler.AuthModelAssembler;
import com.grab.store.identity.internal.api.rest.assembler.UserProfileModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.request.LoginRequest;
import com.grab.store.identity.internal.api.rest.dto.request.LogoutRequest;
import com.grab.store.identity.internal.api.rest.dto.request.RefreshTokenRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.service.AuthCommandService;
import com.grab.store.shared.security.WebMvcSecurityTestConfiguration;
import com.grab.store.shared.security.util.AuthCookieHelper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcSecurityTestConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthCommandService authCommandService;

    @MockBean
    private AuthModelAssembler authModelAssembler;

    @MockBean
    private UserProfileModelAssembler userProfileModelAssembler;

    @MockBean
    private AuthCookieHelper authCookieHelper;

    private ObjectMapper objectMapper;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        authResponse = new AuthResponse(
                "access-token-123",
                "refresh-token-456",
                3600000L,
                "user-1",
                "test@example.com",
                Set.of("CUSTOMER"),
                "ACTIVE",
                false
        );

        when(authModelAssembler.toModel(any(AuthResponse.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "accessToken=access-token-123; Path=/; Secure; HttpOnly; Max-Age=3600; SameSite=Strict");
        headers.add(HttpHeaders.SET_COOKIE, "refreshToken=refresh-token-456; Path=/api/v1/identity/auth/refresh; Secure; HttpOnly; Max-Age=604800; SameSite=Strict");
        when(authCookieHelper.createTokenCookies(any(String.class), any(String.class), any(Long.class))).thenReturn(headers);

        HttpHeaders clearHeaders = new HttpHeaders();
        clearHeaders.add(HttpHeaders.SET_COOKIE, "accessToken=; Path=/; Secure; HttpOnly; Max-Age=0; SameSite=Strict");
        clearHeaders.add(HttpHeaders.SET_COOKIE, "refreshToken=; Path=/api/v1/identity/auth/refresh; Secure; HttpOnly; Max-Age=0; SameSite=Strict");
        when(authCookieHelper.clearTokenCookies()).thenReturn(clearHeaders);
    }

    @Test
    void login_withExplicitContext_shouldReturn200WithCookies() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        when(authCommandService.login(any(LoginRequest.class), any(String.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/identity/auth/login")
                        .header("X-Platform", "SELLER_PORTAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", "access-token-123"))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().maxAge("accessToken", 3600))
                .andExpect(cookie().value("refreshToken", "refresh-token-456"))
                .andExpect(cookie().path("refreshToken", "/api/v1/identity/auth/refresh"))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 604800))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"));
    }

    @Test
    void login_withoutAccessContext_shouldReturn200WithCookies() throws Exception {
        when(authCommandService.login(any(LoginRequest.class), any(String.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/identity/auth/login")
                        .header("X-Platform", "SELLER_PORTAL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"));
    }

    @Test
    void refresh_withCookie_shouldReturn200WithCookies() throws Exception {
        when(authCommandService.refresh(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/identity/auth/refresh")
                        .cookie(new Cookie("refreshToken", "refresh-token-456")))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"));
    }

    @Test
    void refresh_withBody_shouldReturn200WithCookies() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-456");
        when(authCommandService.refresh(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/identity/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"));
    }

    @Test
    void refresh_withoutToken_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/identity/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_withCookie_shouldReturn204AndClearCookies() throws Exception {
        doNothing().when(authCommandService).logout(any(LogoutRequest.class));

        mockMvc.perform(post("/api/v1/identity/auth/logout")
                        .cookie(new Cookie("refreshToken", "refresh-token-456")))
                .andExpect(status().isNoContent())

                .andExpect(cookie().value("accessToken", "")) // Some implementations use null instead of ""
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().httpOnly("accessToken", true))

                .andExpect(cookie().value("refreshToken", "")) // Some implementations use null instead of ""
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(cookie().path("refreshToken", "/api/v1/identity/auth/refresh"))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }
}
