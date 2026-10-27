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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.MerchantLifecycleRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.command.MerchantCommandService;
import com.grab.app.platform.seller.internal.query.MerchantQueryService;
import com.merchant.application.model.write.ChangeMerchantLifecycleCommand.Action;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
import com.grab.app.shared.security.SecurityPrincipal;
import com.merchant.domain.enums.MerchantStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.assembler.MerchantModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@MerchantEnabled
@RequestMapping("/api/v1/admin/merchants")
@RequiredArgsConstructor
public class MerchantAdminController implements SellerRestApi {
    private final MerchantCommandService commands;
    private final MerchantQueryService queries;
    private final MerchantModelAssembler assembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> reviewQueue(
            @RequestParam(defaultValue = "PENDING_REVIEW") MerchantStatus status
    ) {
        List<MerchantResponse> responses = queries.reviewQueue(status);
        CollectionModel<EntityModel<MerchantResponse>> model = assembler.toCollectionModel(responses);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/{merchantId}/request-changes")
    public ResponseEntity<EntityModel<MerchantResponse>> requestChanges(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.REQUEST_CHANGES, request.reason());
    }

    @PostMapping("/{merchantId}/approve")
    public ResponseEntity<EntityModel<MerchantResponse>> approve(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.APPROVE, null);
    }

    @PostMapping("/{merchantId}/reject")
    public ResponseEntity<EntityModel<MerchantResponse>> reject(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.REJECT, request.reason());
    }

    @PostMapping("/{merchantId}/suspend")
    public ResponseEntity<EntityModel<MerchantResponse>> suspend(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.SUSPEND, request.reason());
    }

    @PostMapping("/{merchantId}/reactivate")
    public ResponseEntity<EntityModel<MerchantResponse>> reactivate(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.REACTIVATE, null);
    }

    @PostMapping("/{merchantId}/close")
    public ResponseEntity<EntityModel<MerchantResponse>> close(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantLifecycleRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        return change(merchantId, principal, Action.CLOSE, request.reason());
    }

    private ResponseEntity<EntityModel<MerchantResponse>> change(
            String merchantId, SecurityPrincipal principal, Action action, String reason
    ) {
        String actorId = principal.getPlatformUserId();
        MerchantResponse response = commands.changeLifecycle(merchantId, actorId, action, reason);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }
}
