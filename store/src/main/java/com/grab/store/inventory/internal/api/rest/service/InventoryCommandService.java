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
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.ReceiveStockCommand;
import com.grab.store.inventory.internal.command.ReleaseReservationCommand;
import com.grab.store.inventory.internal.command.ReserveStockCommand;
import com.grab.store.inventory.internal.command.ShipReservationCommand;
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

    public InventoryResponse createInventory(CreateInventoryRequest request, String merchantId, ResolvedInventoryAccess access) {
        CreateInventoryCommand command = createInventoryRequestMapper.toCommand(request, merchantId, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return createInventoryRequestMapper.toResponse(result);
    }

    public InventoryResponse receiveStock(String inventoryItemId, ReceiveStockRequest request, ResolvedInventoryAccess access) {
        ReceiveStockCommand command = receiveStockRequestMapper.toCommand(inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return receiveStockRequestMapper.toResponse(result);
    }

    public InventoryReservationResponse reserveStock(String inventoryItemId, ReserveStockRequest request, String idempotencyKey, ResolvedInventoryAccess access) {
        ReserveStockCommand command = reserveStockRequestMapper.toCommand(inventoryItemId, request, idempotencyKey, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryReservationResult result = commandBus.dispatch(command);
        return reserveStockRequestMapper.toResponse(result);
    }

    public InventoryReservationResponse releaseReservation(String inventoryItemId, String reservationId, ResolvedInventoryAccess access) {
        ReleaseReservationCommand command = releaseReservationRequestMapper.toCommand(inventoryItemId, reservationId, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryReservationResult result = commandBus.dispatch(command);
        return releaseReservationRequestMapper.toResponse(result);
    }

    public InventoryReservationResponse shipReservation(String inventoryItemId, String reservationId, ResolvedInventoryAccess access) {
        ShipReservationCommand command = shipReservationRequestMapper.toCommand(inventoryItemId, reservationId, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryReservationResult result = commandBus.dispatch(command);
        return shipReservationRequestMapper.toResponse(result);
    }

    public InventoryResponse adjustStock(String inventoryItemId, AdjustStockRequest request, ResolvedInventoryAccess access) {
        AdjustStockCommand command = adjustStockRequestMapper.toCommand(inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return adjustStockRequestMapper.toResponse(result);
    }
}
