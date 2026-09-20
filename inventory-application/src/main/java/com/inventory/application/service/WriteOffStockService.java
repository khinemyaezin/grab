package com.inventory.application.service;

import com.inventory.application.port.inbound.WriteOffStockUseCase;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.model.write.WriteOffStockCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WriteOffStockService implements WriteOffStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

            public InventoryItemResult execute(WriteOffStockCommand command) {
        InventoryItem item = requireAccessibleActiveItem(command.inventoryItemId(), command.scopeKey(), command.scopeId());
        StockMovementResult result = inventoryStockService.writeOff(
                item.getId(),
                command.quantity(),
                command.reason() == null || command.reason().isBlank() ? "WRITE_OFF" : command.reason(),
                command.notes(),
                command.createdBy()
        );
        return InventoryItemResults.from(result.item());
    }

        public Class<WriteOffStockCommand> getCommandType() {
        return WriteOffStockCommand.class;
    }

    private InventoryItem requireAccessibleActiveItem(Id inventoryItemId, String scopeKey, String scopeId) {
        InventoryItem item = inventoryRepository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(inventoryItemId.getValue())));
        Location location = locationRepository.findById(item.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(item.getLocationId().getValue())));
        locationAccessPolicy.requireAccess(scopeKey, scopeId, location);
        if (!location.isActive()) {
            throw new InventoryServiceException(new InventoryServiceError.LocationInactive(item.getLocationId().getValue()));
        }
        return item;
    }
}
