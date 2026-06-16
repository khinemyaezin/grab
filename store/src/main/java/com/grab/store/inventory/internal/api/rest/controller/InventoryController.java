package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.assembler.InventoryModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryMovementModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryReservationModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.*;
import com.grab.store.inventory.internal.api.rest.service.InventoryCommandService;
import com.grab.store.inventory.internal.api.rest.service.InventoryQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory/items")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryCommandService inventoryCommandService;
    private final InventoryQueryService inventoryQueryService;
    private final InventoryMovementModelAssembler inventoryMovementModelAssembler;
    private final InventoryModelAssembler inventoryModelAssembler;
    private final InventoryReservationModelAssembler inventoryReservationModelAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        InventoryResponse response = inventoryCommandService.createInventory(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryModelAssembler.toModel(response));
    }

    @GetMapping("/{inventoryItemId}")
    public ResponseEntity<EntityModel<InventoryResponse>> getInventory(
            @PathVariable String inventoryItemId) {
        InventoryResponse response = inventoryQueryService.getInventory(inventoryItemId);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/receive")
    public ResponseEntity<EntityModel<InventoryResponse>> receiveStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReceiveStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        InventoryResponse response = inventoryCommandService.receiveStock(inventoryItemId, request, actorId);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/reserve")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> reserveStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReserveStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        InventoryReservationResponse response = inventoryCommandService.reserveStock(
                inventoryItemId, request, idempotencyKey, actorId);
        return ResponseEntity.ok(inventoryReservationModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/release")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> releaseReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        InventoryReservationResponse response = inventoryCommandService.releaseReservation(inventoryItemId, reservationId, actorId);
        return ResponseEntity.ok(
                inventoryReservationModelAssembler.toModel(response)
        );
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/ship")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> shipReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        InventoryReservationResponse response = inventoryCommandService.shipReservation(inventoryItemId, reservationId, actorId);
        return ResponseEntity.ok( inventoryReservationModelAssembler.toModel(response));
    }

    @PostMapping("/{inventoryItemId}/adjust")
    public ResponseEntity<EntityModel<InventoryResponse>> adjustStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody AdjustStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        InventoryResponse response = inventoryCommandService.adjustStock(inventoryItemId, request, actorId);
        return ResponseEntity.ok(inventoryModelAssembler.toModel(response));
    }

    @GetMapping("/{inventoryItemId}/movements")
    public ResponseEntity<PagedModel<EntityModel<StockMovementResponse>>> getMovements(
            @PathVariable String inventoryItemId,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<StockMovementResponse> pagedAssembler
    ) {
        Page<StockMovementResponse> page = inventoryQueryService.getMovements(inventoryItemId, pageable);
        PagedModel<EntityModel<StockMovementResponse>> pagedModel = pagedAssembler.toModel(page, inventoryMovementModelAssembler);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{inventoryItemId}/reservations")
    public ResponseEntity<PagedModel<EntityModel<InventoryReservationResponse>>> getReservations(
            @PathVariable String inventoryItemId,
            @PageableDefault(size = 20) Pageable pageable,
            PagedResourcesAssembler<InventoryReservationResponse> pagedAssembler
    ) {
        Page<InventoryReservationResponse> page = inventoryQueryService.getReservations(inventoryItemId, pageable);
        PagedModel<EntityModel<InventoryReservationResponse>> pagedModel = pagedAssembler.toModel(page, inventoryReservationModelAssembler);
        return ResponseEntity.ok(pagedModel);
    }
}
