package com.grab.service.sellerbff.internal.adapter.inbound.rest.controller;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.assembler.AccessContextModelAssembler;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.assembler.CurrentUserProfileModelAssembler;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.AccessContextResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.CurrentUserProfileResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.SellerAuthResponse;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.security.oidc.SellerOidcBrowserSession;
import com.grab.framework.security.AccessContext;
import com.grab.framework.security.AuthenticatedActor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.hateoas.RepresentationModel;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerAccessContextCommandService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerAccessContextQueryService;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.service.SellerProfileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity/session")
@RequiredArgsConstructor
public class SellerSessionController {

    private final SellerProfileQueryService profileQueryService;
    private final SellerAccessContextQueryService accessContextQueryService;
    private final SellerAccessContextCommandService accessContextCommandService;
    private final CurrentUserProfileModelAssembler profileModelAssembler;
    private final AccessContextModelAssembler contextModelAssembler;
    private final ObjectProvider<SellerOidcBrowserSession> oidcSessions;

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

    @PostMapping("/contexts/{assignmentId}/select")
    public ResponseEntity<? extends RepresentationModel<?>> selectContext(
            @PathVariable String assignmentId,
            @AuthenticationPrincipal SellerSecurityPrincipal principal,
            HttpServletRequest request) {
        SellerOidcBrowserSession oidc = oidcSessions.getObject();
        String userId = principal.getPlatformUserId();
        AuthenticatedActor selected = oidc.select(request, userId, assignmentId);
        AccessContext context = selected.accessContext();
        AccessContextResponse response = new AccessContextResponse(context.assignmentId(), selected.roles(),
                context.scopeKey(), context.scopeId(), null, null, null);
        return ResponseEntity.ok().body(contextModelAssembler.toModel(response));
    }
}
