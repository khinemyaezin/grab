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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.command.MerchantCommandService;
import com.grab.app.platform.seller.internal.query.MerchantQueryService;
import com.grab.app.account.merchant.internal.config.MerchantEnabled;
import com.grab.app.shared.security.SecurityPrincipal;
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
@RequestMapping("/api/v1/merchants/accounts")
@RequiredArgsConstructor
public class MerchantController implements SellerRestApi {

    private final MerchantCommandService commands;
    private final MerchantQueryService queries;
    private final MerchantModelAssembler assembler;

    @GetMapping()
    public ResponseEntity<CollectionModel<EntityModel<MerchantResponse>>> getMerchants(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        List<MerchantResponse> responses = queries.mine(principal.getPlatformUserId());
        CollectionModel<EntityModel<MerchantResponse>> collection = assembler.toCollectionModel(responses);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/current")
    public ResponseEntity<EntityModel<MerchantResponse>> getCurrentMerchant(
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        MerchantResponse response = queries.current(principal);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<EntityModel<MerchantResponse>> getMerchant(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        MerchantResponse response = queries.get(merchantId, principal);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @PatchMapping("/{merchantId}/profile")
    public ResponseEntity<EntityModel<MerchantResponse>> update(
            @PathVariable String merchantId,
            @Valid @RequestBody UpdateMerchantProfileRequest request,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.update(merchantId, request, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }

    @PostMapping("/{merchantId}/submit")
    public ResponseEntity<EntityModel<MerchantResponse>> submit(
            @PathVariable String merchantId,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String applicantId = principal.getPlatformUserId();
        MerchantResponse response = commands.submit(merchantId, applicantId);
        EntityModel<MerchantResponse> model = assembler.toModel(response);
        return ResponseEntity.ok(model);
    }
}
