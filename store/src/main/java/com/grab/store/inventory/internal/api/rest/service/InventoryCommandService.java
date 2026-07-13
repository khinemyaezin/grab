package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.mapper.AdjustStockRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.CreateInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReceiveStockRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReleaseReservationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReserveStockRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ShipReservationRequestMapper;
import lombok.RequiredArgsConstructor;
import com.grab.store.inventory.internal.api.rest.service.ResolvedInventoryAccess;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryCommandService {

    private final CommandBus commandBus;
    private final CreateInventoryRequestMapper createInventoryRequestMapper;
    private final ReceiveStockRequestMapper receiveStockRequestMapper;
    private final ReserveStockRequestMapper reserveStockRequestMapper;
    private final ReleaseReservationRequestMapper releaseReservationRequestMapper;
    private final ShipReservationRequestMapper shipReservationRequestMapper;
    private final AdjustStockRequestMapper adjustStockRequestMapper;

    public InventoryResponse createInventory(CreateInventoryRequest request, ResolvedInventoryAccess access) {
        var command = createInventoryRequestMapper.toCommand(request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return createInventoryRequestMapper.toResponse(result);
    }

    public InventoryResponse receiveStock(String inventoryItemId, ReceiveStockRequest request, ResolvedInventoryAccess access) {
        var command = receiveStockRequestMapper.toCommand(inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return receiveStockRequestMapper.toResponse(result);
    }

    public InventoryReservationResponse reserveStock(String inventoryItemId, ReserveStockRequest request, String idempotencyKey, ResolvedInventoryAccess access) {
        var command = reserveStockRequestMapper.toCommand(inventoryItemId, request, idempotencyKey, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return reserveStockRequestMapper.toResponse(result);
    }

    public InventoryReservationResponse releaseReservation(String inventoryItemId, String reservationId, ResolvedInventoryAccess access) {
        var command = releaseReservationRequestMapper.toCommand(inventoryItemId, reservationId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return releaseReservationRequestMapper.toResponse(result);
    }

    public InventoryReservationResponse shipReservation(String inventoryItemId, String reservationId, ResolvedInventoryAccess access) {
        var command = shipReservationRequestMapper.toCommand(inventoryItemId, reservationId, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return shipReservationRequestMapper.toResponse(result);
    }

    public InventoryResponse adjustStock(String inventoryItemId, AdjustStockRequest request, ResolvedInventoryAccess access) {
        var command = adjustStockRequestMapper.toCommand(inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        var result = commandBus.dispatch(command);
        return adjustStockRequestMapper.toResponse(result);
    }
}
