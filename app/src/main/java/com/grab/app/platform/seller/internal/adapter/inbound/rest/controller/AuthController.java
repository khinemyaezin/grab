package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.port.rest.SellerRestApi;

import com.grab.app.platform.seller.internal.command.*;
import com.grab.app.platform.seller.internal.query.*;

import com.grab.app.platform.seller.internal.service.AuthenticatedAccessScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedCatalogMerchantResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedMerchantScopeResolver;
import com.grab.app.platform.seller.internal.service.AuthenticatedSalesChannelMerchantResolver;
import com.grab.app.platform.seller.internal.service.PublishableKeyAccessResolver;
import com.grab.app.platform.seller.internal.service.ResolvedInventoryAccess;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.AuthModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.LoginRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.LogoutRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.RegisterRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.RefreshTokenRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.UserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.UserProfileModelAssembler;
import com.grab.app.platform.seller.internal.command.AuthCommandService;
import com.grab.app.shared.security.util.AuthCookieHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
public class AuthController implements SellerRestApi {

    private final AuthCommandService commandService;
    private final AuthModelAssembler authModelAssembler;
    private final UserProfileModelAssembler userProfileModelAssembler;
    private final AuthCookieHelper authCookieHelper;

    @PostMapping("/login")
    public ResponseEntity<EntityModel<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
            ) {
        AuthResponse response = commandService.login(request);
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
            @Valid @RequestBody RegisterRequest request) {
        UserProfileResponse response = commandService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileModelAssembler.toModel(response));
    }
}
