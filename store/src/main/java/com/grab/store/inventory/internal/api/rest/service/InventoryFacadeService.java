package com.grab.store.inventory.internal.api.rest.service;

import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryMovementsResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationsResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryFacadeService {

    private final InventoryCommandService inventoryCommandService;
    private final InventoryQueryService inventoryQueryService;

    public EntityModel<InventoryResponse> createInventory(CreateInventoryRequest request, String actorId) {
        return inventoryCommandService.createInventory(request, actorId);
    }

    public EntityModel<InventoryResponse> getInventory(String inventoryItemId) {
        return inventoryQueryService.getInventory(inventoryItemId);
    }

    public EntityModel<InventoryResponse> receiveStock(String inventoryItemId, ReceiveStockRequest request, String actorId) {
        return inventoryCommandService.receiveStock(inventoryItemId, request, actorId);
    }

    public EntityModel<InventoryReservationResponse> reserveStock(
            String inventoryItemId,
            ReserveStockRequest request,
            String idempotencyKey,
            String actorId
    ) {
        return inventoryCommandService.reserveStock(inventoryItemId, request, idempotencyKey, actorId);
    }

    public EntityModel<InventoryReservationResponse> releaseReservation(
            String inventoryItemId,
            String reservationId,
            String actorId
    ) {
        return inventoryCommandService.releaseReservation(inventoryItemId, reservationId, actorId);
    }

    public EntityModel<InventoryReservationResponse> shipReservation(
            String inventoryItemId,
            String reservationId,
            String actorId
    ) {
        return inventoryCommandService.shipReservation(inventoryItemId, reservationId, actorId);
    }

    public EntityModel<InventoryResponse> adjustStock(String inventoryItemId, AdjustStockRequest request, String actorId) {
        return inventoryCommandService.adjustStock(inventoryItemId, request, actorId);
    }

    public EntityModel<InventoryMovementsResponse> getMovements(String inventoryItemId) {
        return inventoryQueryService.getMovements(inventoryItemId);
    }

    public EntityModel<InventoryReservationsResponse> getReservations(String inventoryItemId) {
        return inventoryQueryService.getReservations(inventoryItemId);
    }
}
