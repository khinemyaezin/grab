package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryModelAssembler;
import com.grab.store.inventory.internal.api.rest.assembler.InventoryReservationModelAssembler;
import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.mapper.AdjustStockDtoMapper;
import com.grab.store.inventory.internal.api.rest.mapper.CreateInventoryDtoMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReceiveStockDtoMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReleaseReservationDtoMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReserveStockDtoMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ShipReservationDtoMapper;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.ReceiveStockCommand;
import com.grab.store.inventory.internal.command.ReleaseReservationCommand;
import com.grab.store.inventory.internal.command.ReserveStockCommand;
import com.grab.store.inventory.internal.command.ShipReservationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryCommandService {

    private final CommandBus commandBus;
    private final CreateInventoryDtoMapper createInventoryDtoMapper;
    private final ReceiveStockDtoMapper receiveStockDtoMapper;
    private final ReserveStockDtoMapper reserveStockDtoMapper;
    private final ReleaseReservationDtoMapper releaseReservationDtoMapper;
    private final ShipReservationDtoMapper shipReservationDtoMapper;
    private final AdjustStockDtoMapper adjustStockDtoMapper;
    private final InventoryModelAssembler inventoryModelAssembler;
    private final InventoryReservationModelAssembler inventoryReservationModelAssembler;

    public EntityModel<InventoryResponse> createInventory(CreateInventoryRequest request, String actorId) {
        CreateInventoryCommand command = createInventoryDtoMapper.toCommand(request, actorId);
        InventoryItemResult result = commandBus.dispatch(command);
        InventoryResponse response = createInventoryDtoMapper.toResponse(result);
        return inventoryModelAssembler.toModel(response);
    }

    public EntityModel<InventoryResponse> receiveStock(String inventoryItemId, ReceiveStockRequest request, String actorId) {
        ReceiveStockCommand command = receiveStockDtoMapper.toCommand(inventoryItemId, request, actorId);
        InventoryItemResult result = commandBus.dispatch(command);
        InventoryResponse response = receiveStockDtoMapper.toResponse(result);
        return inventoryModelAssembler.toModel(response);
    }

    public EntityModel<InventoryReservationResponse> reserveStock(
            String inventoryItemId,
            ReserveStockRequest request,
            String idempotencyKey,
            String actorId
    ) {
        ReserveStockCommand command = reserveStockDtoMapper.toCommand(
                inventoryItemId,
                request,
                idempotencyKey,
                actorId
        );
        InventoryReservationResult result = commandBus.dispatch(command);
        InventoryReservationResponse response = reserveStockDtoMapper.toResponse(result);
        return inventoryReservationModelAssembler.toModel(response);
    }

    public EntityModel<InventoryReservationResponse> releaseReservation(
            String inventoryItemId,
            String reservationId,
            String actorId
    ) {
        ReleaseReservationCommand command = releaseReservationDtoMapper.toCommand(inventoryItemId, reservationId, actorId);
        InventoryReservationResult result = commandBus.dispatch(command);
        InventoryReservationResponse response = releaseReservationDtoMapper.toResponse(result);
        return inventoryReservationModelAssembler.toModel(response);
    }

    public EntityModel<InventoryReservationResponse> shipReservation(
            String inventoryItemId,
            String reservationId,
            String actorId
    ) {
        ShipReservationCommand command = shipReservationDtoMapper.toCommand(inventoryItemId, reservationId, actorId);
        InventoryReservationResult result = commandBus.dispatch(command);
        InventoryReservationResponse response = shipReservationDtoMapper.toResponse(result);
        return inventoryReservationModelAssembler.toModel(response);
    }

    public EntityModel<InventoryResponse> adjustStock(String inventoryItemId, AdjustStockRequest request, String actorId) {
        AdjustStockCommand command = adjustStockDtoMapper.toCommand(inventoryItemId, request, actorId);
        InventoryItemResult result = commandBus.dispatch(command);
        InventoryResponse response = adjustStockDtoMapper.toResponse(result);
        return inventoryModelAssembler.toModel(response);
    }
}
