package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.dto.request.*;
import com.grab.store.identity.internal.api.rest.dto.response.AuthResponse;
import com.grab.store.identity.internal.api.rest.service.AuthCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthCommandService service;

    @PostMapping("/register")
    public ResponseEntity<EntityModel<AuthResponse>> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(service.register(r)));
    }

    @PostMapping("/login")
    public ResponseEntity<EntityModel<AuthResponse>> login(@Valid @RequestBody LoginRequest r) {
        return ResponseEntity.ok(EntityModel.of(service.login(r)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<EntityModel<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest r) {
        return ResponseEntity.ok(EntityModel.of(service.refresh(r)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest r) {
        service.logout(r);
        return ResponseEntity.noContent().build();
    }
}
