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
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request.GrantAccessRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.response.AccessAssignmentResponse;
import com.grab.app.platform.seller.internal.command.AccessCommandService;
import com.grab.app.platform.seller.internal.query.AccessQueryService;
import com.grab.app.shared.security.SecurityPrincipal;
import com.identity.domain.enums.AccessAssignmentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/identity/admin/access-assignments")
@RequiredArgsConstructor
public class AccessAdminController implements SellerRestApi {
    private final AccessCommandService commandService;
    private final AccessQueryService queryService;
    private final AccessAssignmentModelAssembler modelAssembler;

    @GetMapping("/users/{userId}")
    public ResponseEntity<CollectionModel<EntityModel<AccessAssignmentResponse>>> listForUser(
            @PathVariable String userId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        List<AccessAssignmentResponse> responses = queryService.listAssignments(userId, principal);
        CollectionModel<EntityModel<AccessAssignmentResponse>> collectionModel = modelAssembler.toCollectionModel(responses);
        return ResponseEntity.ok(collectionModel);
    }

    @PostMapping
    public ResponseEntity<EntityModel<AccessAssignmentResponse>> grant(
            @Valid @RequestBody GrantAccessRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        AccessAssignmentResponse response = commandService.grant(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelAssembler.toModel(response));
    }

    @PostMapping("/{assignmentId}/suspend")
    public ResponseEntity<EntityModel<AccessAssignmentResponse>> suspend(
            @PathVariable String assignmentId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return changeStatus(assignmentId, AccessAssignmentStatus.SUSPENDED, principal);
    }

    @PostMapping("/{assignmentId}/reactivate")
    public ResponseEntity<EntityModel<AccessAssignmentResponse>> reactivate(
            @PathVariable String assignmentId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return changeStatus(assignmentId, AccessAssignmentStatus.ACTIVE, principal);
    }

    @PostMapping("/{assignmentId}/revoke")
    public ResponseEntity<EntityModel<AccessAssignmentResponse>> revoke(
            @PathVariable String assignmentId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return changeStatus(assignmentId, AccessAssignmentStatus.REVOKED, principal);
    }

    private ResponseEntity<EntityModel<AccessAssignmentResponse>> changeStatus(
            String assignmentId,
            AccessAssignmentStatus requestedStatus,
            SecurityPrincipal principal
    ) {
        AccessAssignmentResponse response = commandService.changeStatus(
                assignmentId, requestedStatus, principal
        );
        return ResponseEntity.ok(modelAssembler.toModel(response));
    }
}
