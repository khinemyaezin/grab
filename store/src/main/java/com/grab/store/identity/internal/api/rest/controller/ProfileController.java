package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.UserProfileModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.response.UserProfileResponse;
import com.grab.store.identity.internal.api.rest.service.IdentityAdminService;
import com.grab.store.shared.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final IdentityAdminService service;
    private final UserProfileModelAssembler userAssembler;

    @GetMapping
    public ResponseEntity<EntityModel<UserProfileResponse>> profile(
            @AuthenticationPrincipal SecurityPrincipal principal) {
        return ResponseEntity.ok(userAssembler.toModel(service.profile(principal.getPlatformUserId())));
    }
}
