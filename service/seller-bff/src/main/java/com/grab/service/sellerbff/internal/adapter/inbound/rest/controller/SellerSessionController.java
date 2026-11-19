package com.grab.service.sellerbff.internal.adapter.inbound.rest.controller;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.assembler.AccessContextModelAssembler;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.assembler.CurrentUserProfileModelAssembler;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.CurrentUserProfileResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcRoutes;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerAccessContextQueryService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerProfileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/identity/session")
@RequiredArgsConstructor
public class SellerSessionController {

    private final SellerProfileQueryService profileQueryService;
    private final SellerAccessContextQueryService accessContextQueryService;
    private final CurrentUserProfileModelAssembler profileModelAssembler;
    private final AccessContextModelAssembler contextModelAssembler;

    @GetMapping
    public ResponseEntity<EntityModel<CurrentUserProfileResponse>> getProfile(
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        return profileQueryService.getProfile(principal)
                .map(profileModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/contexts")
    public ResponseEntity<CollectionModel<EntityModel<AccessContextResponse>>> listContexts(
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        List<AccessContextResponse> responses = accessContextQueryService.listContexts(principal);
        CollectionModel<EntityModel<AccessContextResponse>> collection = contextModelAssembler.toCollectionModel(responses);
        return ResponseEntity.ok(collection);
    }

    @PostMapping("/contexts/switch")
    public ResponseEntity<Void> switchContext() {
        URI target = URI.create(SellerOidcRoutes.AUTHORIZATION_BASE + "/" + SellerOidcRoutes.REGISTRATION_ID + "?prompt=select_context");
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(target).build();
    }

    @GetMapping("/contexts/switch")
    public ResponseEntity<Void> switchContextGet() {
        URI target = URI.create(SellerOidcRoutes.AUTHORIZATION_BASE + "/" + SellerOidcRoutes.REGISTRATION_ID + "?prompt=select_context");
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(target).build();
    }

    @PostMapping("/contexts/{assignmentId}/select")
    public ResponseEntity<Void> selectContext(@PathVariable String assignmentId) {
        URI target = URI.create(SellerOidcRoutes.AUTHORIZATION_BASE + "/" + SellerOidcRoutes.REGISTRATION_ID + "?prompt=select_context");
        return ResponseEntity.status(HttpStatus.SEE_OTHER).location(target).build();
    }
}
