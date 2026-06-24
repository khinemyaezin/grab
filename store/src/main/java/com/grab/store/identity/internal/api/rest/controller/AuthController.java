package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.AuthModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.request.LoginRequest;
import com.grab.store.identity.internal.api.rest.dto.request.LogoutRequest;
import com.grab.store.identity.internal.api.rest.dto.request.RefreshTokenRequest;
import com.grab.store.identity.internal.api.rest.dto.request.RegisterUserRequest;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.service.AuthCommandService;
import com.grab.store.identity.internal.api.rest.util.AuthCookieHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandService commandService;
    private final AuthModelAssembler authModelAssembler;
    private final AuthCookieHelper authCookieHelper;

    @PostMapping("/register")
    public ResponseEntity<EntityModel<AuthResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        AuthResponse response = commandService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(authCookieHelper.createTokenCookies(response))
                .body(authModelAssembler.toModel(response));
    }

    @PostMapping("/login")
    public ResponseEntity<EntityModel<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = commandService.login(request);
        return ResponseEntity.ok()
                .headers(authCookieHelper.createTokenCookies(response))
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
                .headers(authCookieHelper.createTokenCookies(response))
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
}
