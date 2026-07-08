package com.grab.store.identity.internal.api.rest.controller;

import com.grab.store.identity.internal.api.rest.assembler.CurrentUserProfileModelAssembler;
import com.grab.store.identity.internal.api.rest.dto.response.CurrentUserProfileResponse;
import com.grab.store.identity.internal.api.rest.service.ProfileQueryService;
import com.grab.store.shared.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/session")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileQueryService queryService;
    private final CurrentUserProfileModelAssembler profileModelAssembler;

    @GetMapping
    public ResponseEntity<EntityModel<CurrentUserProfileResponse>> getProfile(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        CurrentUserProfileResponse response = queryService.getProfile(principal.getPlatformUserId(), principal);
        return ResponseEntity.ok(profileModelAssembler.toModel(response));
    }
}
