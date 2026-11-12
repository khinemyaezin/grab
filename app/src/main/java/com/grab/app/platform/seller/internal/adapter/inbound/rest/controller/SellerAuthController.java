package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SellerAuthModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SellerUserProfileModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.util.SellerAuthCookieHelper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLoginRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerLogoutRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerRefreshTokenRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.SellerRegisterRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerAuthCommandService;
import com.grab.app.platform.seller.port.rest.SellerAuthResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/auth")
public class SellerAuthController implements SellerAuthResource {

    private final SellerAuthCommandService commandService;
    private final SellerAuthModelAssembler authModelAssembler;
    private final SellerUserProfileModelAssembler userProfileModelAssembler;
    private final SellerAuthCookieHelper authCookieHelper;

    public SellerAuthController(
            SellerAuthCommandService commandService,
            SellerAuthModelAssembler authModelAssembler,
            SellerUserProfileModelAssembler userProfileModelAssembler,
            SellerAuthCookieHelper authCookieHelper) {
        this.commandService = commandService;
        this.authModelAssembler = authModelAssembler;
        this.userProfileModelAssembler = userProfileModelAssembler;
        this.authCookieHelper = authCookieHelper;
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<EntityModel<SellerAuthResponse>> login(@Valid @RequestBody SellerLoginRequest request) {
        SellerAuthResponse response = commandService.login(request);
        HttpHeaders headers = authCookieHelper.createTokenCookies(
                response.accessToken(),
                response.refreshToken(),
                response.expiresInMs()
        );
        return ResponseEntity.ok()
                .headers(headers)
                .body(authModelAssembler.toModel(response));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<EntityModel<SellerAuthResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) SellerRefreshTokenRequest request) {
        String token = (refreshTokenCookie != null && !refreshTokenCookie.isBlank())
                ? refreshTokenCookie
                : (request != null ? request.refreshToken() : null);

        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SellerAuthResponse response = commandService.refresh(new SellerRefreshTokenRequest(token));
        HttpHeaders headers = authCookieHelper.createTokenCookies(
                response.accessToken(),
                response.refreshToken(),
                response.expiresInMs()
        );
        return ResponseEntity.ok()
                .headers(headers)
                .body(authModelAssembler.toModel(response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) SellerLogoutRequest request) {
        String token = (refreshTokenCookie != null && !refreshTokenCookie.isBlank())
                ? refreshTokenCookie
                : (request != null ? request.refreshToken() : null);

        if (token != null && !token.isBlank()) {
            commandService.logout(new SellerLogoutRequest(token));
        }

        HttpHeaders headers = authCookieHelper.clearTokenCookies();
        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<EntityModel<SellerUserProfileResponse>> register(@Valid @RequestBody SellerRegisterRequest request) {
        SellerUserProfileResponse response = commandService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileModelAssembler.toModel(response));
    }
}

