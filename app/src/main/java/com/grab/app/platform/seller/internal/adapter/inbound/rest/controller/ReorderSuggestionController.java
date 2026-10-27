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

import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.assembler.ReorderSuggestionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.ReorderSuggestionResponse;
import com.grab.app.platform.seller.internal.service.AuthenticatedInventoryScopeResolver;
import com.grab.app.platform.seller.internal.query.InventoryQueryService;
import com.grab.app.shared.security.SecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/reorder-suggestions")
@RequiredArgsConstructor
public class ReorderSuggestionController implements SellerRestApi {

    private final InventoryQueryService inventoryQueryService;
    private final ReorderSuggestionModelAssembler reorderSuggestionModelAssembler;
    private final AuthenticatedInventoryScopeResolver scopeResolver;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ReorderSuggestionResponse>>> list(
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String sku,
            @AuthenticationPrincipal SecurityPrincipal principal
    ) {
        String merchantId = scopeResolver.resolveOwnerMerchantId(principal);
        List<ReorderSuggestionResponse> responses = inventoryQueryService.getReorderSuggestions(merchantId, locationId, sku);
        return ResponseEntity.ok(reorderSuggestionModelAssembler.toCollectionModel(responses, locationId, sku));
    }
}
