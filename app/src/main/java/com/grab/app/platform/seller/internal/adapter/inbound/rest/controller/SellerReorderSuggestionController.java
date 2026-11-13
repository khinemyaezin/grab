package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.ReorderSuggestionModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ReorderSuggestionResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/reorder-suggestions")
@RequiredArgsConstructor
public class SellerReorderSuggestionController {

    private final SellerInventoryQueryService queryService;
    private final ReorderSuggestionModelAssembler modelAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ReorderSuggestionResponse>>> listSuggestions(
            @AuthenticationPrincipal SellerSecurityPrincipal principal,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String sku) {
        List<ReorderSuggestionResponse> results = queryService.listReorderSuggestions(
                principal.requiredScopeId(), locationId, sku);
        return ResponseEntity.ok(modelAssembler.listSuggestionsModel(results, locationId, sku));
    }
}
