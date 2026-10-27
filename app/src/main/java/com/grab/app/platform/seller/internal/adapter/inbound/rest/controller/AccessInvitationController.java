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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.AccessAssignmentModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.assembler.AccessInvitationModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.AcceptAccessInvitationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.CreateAccessInvitationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessInvitationResponse;
import com.grab.app.platform.seller.internal.command.AccessInvitationCommandService;
import com.grab.app.shared.security.SecurityPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/access-invitations")
@RequiredArgsConstructor
public class AccessInvitationController implements SellerRestApi {
    private final AccessInvitationCommandService commandService;
    private final AccessInvitationModelAssembler invitationModelAssembler;
    private final AccessAssignmentModelAssembler assignmentModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<AccessInvitationResponse>> create(
            @Valid @RequestBody CreateAccessInvitationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AccessInvitationResponse response = commandService.create(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationModelAssembler.toModel(response));
    }

    @PostMapping("/accept")
    public ResponseEntity<EntityModel<AccessAssignmentResponse>> accept(
            @Valid @RequestBody AcceptAccessInvitationRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AccessAssignmentResponse response = commandService.accept(request, principal);
        return ResponseEntity.ok(assignmentModelAssembler.toModel(response));
    }

    @PostMapping("/{invitationId}/cancel")
    public ResponseEntity<EntityModel<AccessInvitationResponse>> cancel(
            @PathVariable String invitationId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AccessInvitationResponse response = commandService.cancel(invitationId, principal);
        return ResponseEntity.ok(invitationModelAssembler.toModel(response));
    }
}
