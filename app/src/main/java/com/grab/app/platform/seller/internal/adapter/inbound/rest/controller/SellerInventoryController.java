package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.InventoryItemModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateInventoryItemRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StockMovementResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.security.principal.SellerSecurityPrincipal;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/inventory/items")
@RequiredArgsConstructor

public class SellerInventoryController {

    private final SellerInventoryCommandService commandService;
    private final SellerInventoryQueryService queryService;
    private final InventoryItemModelAssembler modelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<InventoryItemResponse>> createItem(
            @Valid @RequestBody CreateInventoryItemRequest request,
            @AuthenticationPrincipal SellerSecurityPrincipal principal) {
        InventoryItemResponse response = commandService.createItem(
                principal.requiredScopeId(),
                principal.getPlatformUserId(),
                request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toCommandModel(response));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<EntityModel<InventoryItemResponse>> getItem(@PathVariable String itemId) {
        return queryService.getItem(itemId)
                .map(modelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{itemId}/movements/latest")
    public ResponseEntity<EntityModel<StockMovementResponse>> getLatestMovement(@PathVariable String itemId) {
        return queryService.getLatestMovement(itemId)
                .map(EntityModel::of)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
