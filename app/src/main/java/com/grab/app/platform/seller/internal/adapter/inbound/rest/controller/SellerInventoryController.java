package com.grab.app.platform.seller.internal.adapter.inbound.rest.controller;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.assembler.InventoryItemModelAssembler;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateInventoryItemRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StockMovementResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryCommandService;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.service.SellerInventoryQueryService;
import com.grab.app.platform.seller.port.rest.SellerInventoryResource;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/inventory/items")
public class SellerInventoryController implements SellerInventoryResource {

    private final SellerInventoryCommandService commandService;
    private final SellerInventoryQueryService queryService;
    private final InventoryItemModelAssembler modelAssembler;

    public SellerInventoryController(
            SellerInventoryCommandService commandService,
            SellerInventoryQueryService queryService,
            InventoryItemModelAssembler modelAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.modelAssembler = modelAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<InventoryItemResponse>> createItem(
            @Valid @RequestBody CreateInventoryItemRequest request,
            @RequestHeader(value = "X-Merchant-Id", required = false) String merchantId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId) {
        InventoryItemResponse response = commandService.createItem(merchantId, actorId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(modelAssembler.toModel(response));
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
