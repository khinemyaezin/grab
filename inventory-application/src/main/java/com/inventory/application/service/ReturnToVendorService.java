package com.inventory.application.service;

import com.inventory.application.port.inbound.ReturnToVendorUseCase;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.model.write.ReturnToVendorCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReturnToVendorService implements ReturnToVendorUseCase {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

            public InventoryItemResult execute(ReturnToVendorCommand command) {
        InventoryItem item = requireAccessibleActiveItem(command.inventoryItemId(), command.scopeKey(), command.scopeId());
        StockMovementResult result = inventoryStockService.returnToVendor(
                item.getId(),
                command.quantity(),
                command.reason() == null || command.reason().isBlank() ? "RETURN_TO_VENDOR" : command.reason(),
                command.notes(),
                command.createdBy()
        );
        return InventoryItemResults.from(result.item());
    }

        public Class<ReturnToVendorCommand> getCommandType() {
        return ReturnToVendorCommand.class;
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
