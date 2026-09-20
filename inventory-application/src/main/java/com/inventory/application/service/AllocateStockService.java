package com.inventory.application.service;

import com.inventory.application.port.inbound.AllocateStockUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.port.outbound.InventoryReservationRepository;
import com.inventory.domain.service.InventoryAllocationService;
import com.inventory.domain.service.InventoryAllocationService.AllocationDetail;
import com.inventory.domain.service.InventoryAllocationService.AllocationResult;
import com.inventory.application.model.write.AllocateStockCommand;
import com.inventory.application.model.write.AllocateStockResult;
import com.inventory.application.model.write.AllocateStockResult.AllocationLineResult;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AllocateStockService implements AllocateStockUseCase {

    private final InventoryAllocationService inventoryAllocationService;
    private final InventoryReservationRepository inventoryReservationQueryPort;
    private final ProductVariantViewQueryPort productVariantViewQueryPort;
    private final IdGenerator idGenerator;

            public AllocateStockResult execute(AllocateStockCommand command) {
        if (isUntracked(command.sku())) {
            return new AllocateStockResult(
                    true,
                    command.sku(),
                    command.quantity(),
                    command.quantity(),
                    command.orderId(),
                    List.of(),
                    null,
                    null
            );
        }

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
            inventoryReservationQueryPort.save(reservation);
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

        public Class<AllocateStockCommand> getCommandType() {
        return AllocateStockCommand.class;
    }

    private boolean isUntracked(String sku) {
        return productVariantViewQueryPort.findActiveBySku(sku)
                .map(view -> !view.isManageInventory())
                .orElse(false);
    }
}
