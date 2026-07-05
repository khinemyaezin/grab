package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.AuthModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.request.LoginRequest;
import com.grab.store.identity.internal.api.rest.dto.request.LogoutRequest;
import com.grab.store.identity.internal.api.rest.dto.request.RegisterRequest;
import com.grab.store.identity.internal.api.rest.dto.request.RefreshTokenRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.assembler.UserProfileModelAssembler;
import com.grab.store.identity.internal.api.rest.service.AuthCommandService;
import com.grab.store.shared.security.util.AuthCookieHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandService commandService;
    private final AuthModelAssembler authModelAssembler;
    private final UserProfileModelAssembler userProfileModelAssembler;
    private final AuthCookieHelper authCookieHelper;

    @PostMapping("/login")
    public ResponseEntity<EntityModel<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Platform") String platformCode
            ) {
        AuthResponse response = commandService.login(request, platformCode);
        return ResponseEntity.ok()
                .headers(authCookieHelper.createTokenCookies(response.accessToken(), response.refreshToken(), response.expiresInMs()))
                .body(authModelAssembler.toModel(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<EntityModel<AuthResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) RefreshTokenRequest request) {

        String token = refreshTokenCookie != null ? refreshTokenCookie : (request != null ? request.refreshToken() : null);
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        AuthResponse response = commandService.refresh(new RefreshTokenRequest(token));
        return ResponseEntity.ok()
                .headers(authCookieHelper.createTokenCookies(response.accessToken(), response.refreshToken(), response.expiresInMs()))
                .body(authModelAssembler.toModel(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) LogoutRequest request) {

        String token = refreshTokenCookie != null ? refreshTokenCookie : (request != null ? request.refreshToken() : null);
        if (token != null && !token.isBlank()) {
            commandService.logout(new LogoutRequest(token));
        }

        return ResponseEntity.noContent()
                .headers(authCookieHelper.clearTokenCookies())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<EntityModel<UserProfileResponse>> register(
            @RequestHeader(value = "X-Platform") String platformCode,
            @Valid @RequestBody RegisterRequest request) {
        UserProfileResponse response = commandService.register(request, platformCode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileModelAssembler.toModel(response));
    }
}
