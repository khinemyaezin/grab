package com.inventory.application.service;

import com.inventory.application.port.inbound.ReceiveStockUseCase;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.model.write.ReceiveStockCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReceiveStockService implements ReceiveStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

            public InventoryItemResult execute(ReceiveStockCommand command) {
        
        InventoryItem item = inventoryRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(command.inventoryItemId().getValue())));

        Location location = locationRepository.findById(item.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(item.getLocationId().getValue())));

        locationAccessPolicy.requireAccess(command.scopeKey(), command.scopeId(), location);
        if (!location.isActive()) {
            throw new InventoryServiceException(new InventoryServiceError.LocationInactive(item.getLocationId().getValue()));
        }

        StockMovementResult result = inventoryStockService.receiveStock(
                command.inventoryItemId(),
                command.quantity(),
                command.type(),
                command.referenceId(),
                null,
                command.createdBy()
        );

        return InventoryItemResults.from(result.item());
    }

        public Class<ReceiveStockCommand> getCommandType() {
        return ReceiveStockCommand.class;
    }
}
