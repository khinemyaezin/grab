package com.inventory.application.service;

import com.inventory.application.port.inbound.AdjustStockUseCase;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.inventory.application.model.write.AdjustStockCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdjustStockService implements AdjustStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

            public InventoryItemResult execute(AdjustStockCommand command) {
        
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

        StockMovementResult result = inventoryStockService.adjustStock(
                command.inventoryItemId(),
                command.newOnHandQuantity(),
                command.reason(),
                null,
                command.createdBy()
        );

        return InventoryItemResults.from(result.item());
    }

        public Class<AdjustStockCommand> getCommandType() {
        return AdjustStockCommand.class;
    }
}
