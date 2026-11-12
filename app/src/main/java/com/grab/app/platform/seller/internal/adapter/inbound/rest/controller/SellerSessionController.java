package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.AccessContextModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.CurrentUserProfileModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.SellerAuthModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.CurrentUserProfileResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.util.SellerAuthCookieHelper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerAccessContextCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerAccessContextQueryService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerProfileQueryService;
import com.grab.app.platform.seller.port.rest.SellerSessionResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity/session")
public class SellerSessionController implements SellerSessionResource {

    private final SellerProfileQueryService profileQueryService;
    private final SellerAccessContextQueryService accessContextQueryService;
    private final SellerAccessContextCommandService accessContextCommandService;
    private final CurrentUserProfileModelAssembler profileModelAssembler;
    private final AccessContextModelAssembler contextModelAssembler;
    private final SellerAuthModelAssembler authModelAssembler;
    private final SellerAuthCookieHelper authCookieHelper;

    public SellerSessionController(
            SellerProfileQueryService profileQueryService,
            SellerAccessContextQueryService accessContextQueryService,
            SellerAccessContextCommandService accessContextCommandService,
            CurrentUserProfileModelAssembler profileModelAssembler,
            AccessContextModelAssembler contextModelAssembler,
            SellerAuthModelAssembler authModelAssembler,
            SellerAuthCookieHelper authCookieHelper
    ) {
        this.profileQueryService = profileQueryService;
        this.accessContextQueryService = accessContextQueryService;
        this.accessContextCommandService = accessContextCommandService;
        this.profileModelAssembler = profileModelAssembler;
        this.contextModelAssembler = contextModelAssembler;
        this.authModelAssembler = authModelAssembler;
        this.authCookieHelper = authCookieHelper;
    }

    @Override
    @GetMapping
    public ResponseEntity<EntityModel<CurrentUserProfileResponse>> getProfile(
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        return profileQueryService.getProfile(principal)
                .map(profileModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/contexts")
    public ResponseEntity<CollectionModel<EntityModel<AccessContextResponse>>> listContexts(
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        List<AccessContextResponse> responses = accessContextQueryService.listContexts(principal);
        CollectionModel<EntityModel<AccessContextResponse>> collection = contextModelAssembler.toCollectionModel(responses);
        return ResponseEntity.ok(collection);
    }

    @Override
    @PostMapping("/contexts/{assignmentId}/select")
    public ResponseEntity<EntityModel<SellerAuthResponse>> selectContext(
            @PathVariable String assignmentId,
            @CookieValue(name = "refreshToken", required = false) String currentRefreshToken,
            @AuthenticationPrincipal SellerSecurityPrincipal principal
    ) {
        SellerAuthResponse response = accessContextCommandService.switchContext(assignmentId, currentRefreshToken, principal);
        HttpHeaders headers = authCookieHelper.createTokenCookies(
                response.accessToken(),
                response.refreshToken(),
                response.expiresInMs()
        );
        return ResponseEntity.ok()
                .headers(headers)
                .body(authModelAssembler.toModel(response));
    }
}

