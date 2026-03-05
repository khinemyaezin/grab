package com.grab.store.inventory.internal.api.rest.controller;

import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryMovementsResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationsResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.service.InventoryFacadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryFacadeService inventoryFacadeService;

    @PostMapping
    public ResponseEntity<EntityModel<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryFacadeService.createInventory(request, actorId));
    }

    @GetMapping("/{inventoryItemId}")
    public ResponseEntity<EntityModel<InventoryResponse>> getInventory(@PathVariable String inventoryItemId) {
        return ResponseEntity.ok(inventoryFacadeService.getInventory(inventoryItemId));
    }

    @PostMapping("/{inventoryItemId}/receive")
    public ResponseEntity<EntityModel<InventoryResponse>> receiveStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReceiveStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(inventoryFacadeService.receiveStock(inventoryItemId, request, actorId));
    }

    @PostMapping("/{inventoryItemId}/reserve")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> reserveStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody ReserveStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.ok(inventoryFacadeService.reserveStock(
                inventoryItemId, request, idempotencyKey, actorId));
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/release")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> releaseReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(
                inventoryFacadeService.releaseReservation(inventoryItemId, reservationId, actorId));
    }

    @PostMapping("/{inventoryItemId}/reservations/{reservationId}/ship")
    public ResponseEntity<EntityModel<InventoryReservationResponse>> shipReservation(
            @PathVariable String inventoryItemId,
            @PathVariable String reservationId,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(
                inventoryFacadeService.shipReservation(inventoryItemId, reservationId, actorId));
    }

    @PostMapping("/{inventoryItemId}/adjust")
    public ResponseEntity<EntityModel<InventoryResponse>> adjustStock(
            @PathVariable String inventoryItemId,
            @Valid @RequestBody AdjustStockRequest request,
            @RequestHeader(value = "X-Actor-Id", required = false) String actorId
    ) {
        return ResponseEntity.ok(inventoryFacadeService.adjustStock(inventoryItemId, request, actorId));
    }

    @GetMapping("/{inventoryItemId}/movements")
    public ResponseEntity<EntityModel<InventoryMovementsResponse>> getMovements(@PathVariable String inventoryItemId) {
        return ResponseEntity.ok(inventoryFacadeService.getMovements(inventoryItemId));
    }

    @GetMapping("/{inventoryItemId}/reservations")
    public ResponseEntity<EntityModel<InventoryReservationsResponse>> getReservations(@PathVariable String inventoryItemId) {
        return ResponseEntity.ok(inventoryFacadeService.getReservations(inventoryItemId));
    }
}
