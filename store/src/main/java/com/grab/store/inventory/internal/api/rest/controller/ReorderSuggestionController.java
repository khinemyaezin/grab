package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.assembler.ReorderSuggestionModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.response.ReorderSuggestionResponse;
import com.grab.store.inventory.internal.api.rest.service.AuthenticatedInventoryScopeResolver;
import com.grab.store.inventory.internal.api.rest.service.InventoryQueryService;
import com.grab.store.shared.security.SecurityPrincipal;
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
public class ReorderSuggestionController {

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
