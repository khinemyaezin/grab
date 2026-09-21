package com.inventory.application.service;

import com.inventory.application.port.inbound.UpdateReorderConfigUseCase;

import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.port.outbound.InventoryRepository;
import com.inventory.domain.port.outbound.LocationRepository;
import com.inventory.domain.valueobject.ReorderConfig;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.InventoryItemResults;
import com.inventory.application.model.write.UpdateReorderConfigCommand;
import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.domain.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateReorderConfigService implements UpdateReorderConfigUseCase {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

            public InventoryItemResult execute(UpdateReorderConfigCommand command) {
        InventoryItem item = requireAccessibleItem(command.inventoryItemId(), command.scopeKey(), command.scopeId());
        item.updateReorderConfig(new ReorderConfig(
                command.safetyStock(),
                command.reorderPoint(),
                command.reorderQuantity(),
                command.maxStock()
        ));
        inventoryRepository.save(item);
        return InventoryItemResults.from(item);
    }

        public Class<UpdateReorderConfigCommand> getCommandType() {
        return UpdateReorderConfigCommand.class;
    }

    private InventoryItem requireAccessibleItem(com.grab.framework.id.Id inventoryItemId, String scopeKey, String scopeId) {
        InventoryItem item = inventoryRepository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(inventoryItemId.getValue())));
        Location location = locationRepository.findById(item.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(item.getLocationId().getValue())));
        locationAccessPolicy.requireAccess(scopeKey, scopeId, location);
        return item;
    }
}
