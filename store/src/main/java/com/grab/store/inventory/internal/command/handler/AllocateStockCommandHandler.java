package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.domain.service.InventoryAllocationService.AllocationDetail;
import com.inventory.domain.service.InventoryAllocationService.AllocationResult;
import com.grab.store.inventory.internal.command.AllocateStockCommand;
import com.grab.store.inventory.internal.command.AllocateStockResult;
import com.grab.store.inventory.internal.command.AllocateStockResult.AllocationLineResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AllocateStockCommandHandler implements CommandHandler<AllocateStockCommand, AllocateStockResult> {

    private final InventoryAllocationService inventoryAllocationService;
    private final InventoryReservationRepository inventoryReservationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public AllocateStockResult handle(AllocateStockCommand command) {
        AllocationResult result;
        if (command.locationId() != null) {
            result = inventoryAllocationService.allocateStockFromLocation(
                    command.sku(),
                    command.locationId(),
                    command.quantity(),
                    command.orderId(),
                    command.createdBy()
            );
        } else {
            result = inventoryAllocationService.allocateStock(
                    command.sku(),
                    command.quantity(),
                    command.orderId(),
                    command.createdBy()
            );
        }

        if (!result.success()) {
            throw new InventoryServiceException(new InventoryServiceError.AllocationFailed(
                    command.sku(),
                    command.quantity(),
                    result.error() == null ? "allocation_failed" : result.error().code()
            ));
        }

        String orderLineId = command.orderLineId() == null || command.orderLineId().isBlank()
                ? command.orderId()
                : command.orderLineId();

        List<AllocationLineResult> lines = new ArrayList<>();
        for (AllocationDetail detail : result.allocations()) {
            InventoryReservation reservation = InventoryReservation.create(
                    idGenerator.generateId(),
                    detail.inventoryItemId(),
                    command.orderId(),
                    orderLineId,
                    detail.quantity(),
                    command.expiresAt(),
                    null
            );
            inventoryReservationRepository.save(reservation);
            lines.add(new AllocationLineResult(
                    reservation.getId().getValue(),
                    detail.inventoryItemId().getValue(),
                    detail.locationId().getValue(),
                    detail.quantity()
            ));
        }

        return new AllocateStockResult(
                true,
                command.sku(),
                command.quantity(),
                result.allocatedQuantity(),
                command.orderId(),
                lines,
                null,
                null
        );
    }

    @Override
    public Class<AllocateStockCommand> getCommandType() {
        return AllocateStockCommand.class;
    }
}
