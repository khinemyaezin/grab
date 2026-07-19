package com.grab.store.inventory.internal.api.rest.service;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AllocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.AnnounceInTransitRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.DeallocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.MarkDamagedRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveInTransitRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.ReturnToVendorRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.TransferInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateReorderConfigRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.WriteOffStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocateStockResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.DeallocateStockResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.TransferInventoryResponse;
import com.grab.store.inventory.internal.api.rest.mapper.AdjustStockRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.AllocationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.AnnounceInTransitRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.CreateInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.InventoryLifecycleRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.MarkDamagedRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReceiveInTransitRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReceiveStockRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReleaseReservationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReserveStockRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ReturnToVendorRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.ShipReservationRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.TransferInventoryRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.UpdateReorderConfigRequestMapper;
import com.grab.store.inventory.internal.api.rest.mapper.WriteOffStockRequestMapper;
import com.grab.store.inventory.internal.command.ActivateInventoryCommand;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.AllocateStockCommand;
import com.grab.store.inventory.internal.command.AllocateStockResult;
import com.grab.store.inventory.internal.command.AnnounceInTransitCommand;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.DeallocateStockCommand;
import com.grab.store.inventory.internal.command.DeallocateStockResult;
import com.grab.store.inventory.internal.command.DiscontinueInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.MarkDamagedCommand;
import com.grab.store.inventory.internal.command.ReceiveInTransitCommand;
import com.grab.store.inventory.internal.command.ReceiveStockCommand;
import com.grab.store.inventory.internal.command.ReleaseReservationCommand;
import com.grab.store.inventory.internal.command.ReserveStockCommand;
import com.grab.store.inventory.internal.command.ReturnToVendorCommand;
import com.grab.store.inventory.internal.command.ShipReservationCommand;
import com.grab.store.inventory.internal.command.SuspendInventoryCommand;
import com.grab.store.inventory.internal.command.TransferInventoryCommand;
import com.grab.store.inventory.internal.command.TransferInventoryResult;
import com.grab.store.inventory.internal.command.UpdateReorderConfigCommand;
import com.grab.store.inventory.internal.command.WriteOffStockCommand;
import lombok.RequiredArgsConstructor;
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
    private final MarkDamagedRequestMapper markDamagedRequestMapper;
    private final WriteOffStockRequestMapper writeOffStockRequestMapper;
    private final ReturnToVendorRequestMapper returnToVendorRequestMapper;
    private final InventoryLifecycleRequestMapper inventoryLifecycleRequestMapper;
    private final TransferInventoryRequestMapper transferInventoryRequestMapper;
    private final AllocationRequestMapper allocationRequestMapper;
    private final AnnounceInTransitRequestMapper announceInTransitRequestMapper;
    private final ReceiveInTransitRequestMapper receiveInTransitRequestMapper;
    private final UpdateReorderConfigRequestMapper updateReorderConfigRequestMapper;

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

    public InventoryResponse markDamaged(String inventoryItemId, MarkDamagedRequest request, ResolvedInventoryAccess access) {
        MarkDamagedCommand command = markDamagedRequestMapper.toCommand(inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return markDamagedRequestMapper.toResponse(result);
    }

    public InventoryResponse writeOff(String inventoryItemId, WriteOffStockRequest request, ResolvedInventoryAccess access) {
        WriteOffStockCommand command = writeOffStockRequestMapper.toCommand(inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return writeOffStockRequestMapper.toResponse(result);
    }

    public InventoryResponse returnToVendor(String inventoryItemId, ReturnToVendorRequest request, ResolvedInventoryAccess access) {
        ReturnToVendorCommand command = returnToVendorRequestMapper.toCommand(inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return returnToVendorRequestMapper.toResponse(result);
    }

    public InventoryResponse suspend(String inventoryItemId, ResolvedInventoryAccess access) {
        SuspendInventoryCommand command = inventoryLifecycleRequestMapper.toSuspendCommand(
                inventoryItemId, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return inventoryLifecycleRequestMapper.toResponse(result);
    }

    public InventoryResponse activate(String inventoryItemId, ResolvedInventoryAccess access) {
        ActivateInventoryCommand command = inventoryLifecycleRequestMapper.toActivateCommand(
                inventoryItemId, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return inventoryLifecycleRequestMapper.toResponse(result);
    }

    public InventoryResponse discontinue(String inventoryItemId, ResolvedInventoryAccess access) {
        DiscontinueInventoryCommand command = inventoryLifecycleRequestMapper.toDiscontinueCommand(
                inventoryItemId, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return inventoryLifecycleRequestMapper.toResponse(result);
    }

    public TransferInventoryResponse transfer(String inventoryItemId, TransferInventoryRequest request, ResolvedInventoryAccess access) {
        TransferInventoryCommand command = transferInventoryRequestMapper.toCommand(
                inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        TransferInventoryResult result = commandBus.dispatch(command);
        return transferInventoryRequestMapper.toResponse(result);
    }

    public AllocateStockResponse allocateStock(AllocateStockRequest request, ResolvedInventoryAccess access) {
        AllocateStockCommand command = allocationRequestMapper.toAllocateCommand(
                request, access.actorId(), access.scopeKey(), access.scopeId());
        AllocateStockResult result = commandBus.dispatch(command);
        return allocationRequestMapper.toAllocateResponse(result);
    }

    public DeallocateStockResponse deallocateStock(DeallocateStockRequest request, ResolvedInventoryAccess access) {
        DeallocateStockCommand command = allocationRequestMapper.toDeallocateCommand(
                request, access.actorId(), access.scopeKey(), access.scopeId());
        DeallocateStockResult result = commandBus.dispatch(command);
        return allocationRequestMapper.toDeallocateResponse(result);
    }

    public InventoryResponse announceInTransit(String inventoryItemId, AnnounceInTransitRequest request, ResolvedInventoryAccess access) {
        AnnounceInTransitCommand command = announceInTransitRequestMapper.toCommand(
                inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return announceInTransitRequestMapper.toResponse(result);
    }

    public InventoryResponse receiveInTransit(String inventoryItemId, ReceiveInTransitRequest request, ResolvedInventoryAccess access) {
        ReceiveInTransitCommand command = receiveInTransitRequestMapper.toCommand(
                inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return receiveInTransitRequestMapper.toResponse(result);
    }

    public InventoryResponse updateReorderConfig(String inventoryItemId, UpdateReorderConfigRequest request, ResolvedInventoryAccess access) {
        UpdateReorderConfigCommand command = updateReorderConfigRequestMapper.toCommand(
                inventoryItemId, request, access.actorId(), access.scopeKey(), access.scopeId());
        InventoryItemResult result = commandBus.dispatch(command);
        return updateReorderConfigRequestMapper.toResponse(result);
    }
}
