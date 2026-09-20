package com.inventory.application.service;

import com.inventory.application.port.inbound.AnnounceInTransitUseCase;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.inventory.application.model.write.AnnounceInTransitCommand;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnnounceInTransitService implements AnnounceInTransitUseCase {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

            public InventoryItemResult execute(AnnounceInTransitCommand command) {
        InventoryItem item = requireAccessibleActiveItem(command.inventoryItemId(), command.scopeKey(), command.scopeId());
        StockMovementResult result = inventoryStockService.announceInTransit(
                item.getId(), command.quantity(), command.referenceId(), command.createdBy());
        return InventoryItemResults.from(result.item());
    }

        public Class<AnnounceInTransitCommand> getCommandType() {
        return AnnounceInTransitCommand.class;
    }

    private InventoryItem requireAccessibleActiveItem(com.grab.framework.id.Id inventoryItemId, String scopeKey, String scopeId) {
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
