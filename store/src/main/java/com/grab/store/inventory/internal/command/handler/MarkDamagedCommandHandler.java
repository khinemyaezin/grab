package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.InventoryItemResults;
import com.grab.store.inventory.internal.command.MarkDamagedCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarkDamagedCommandHandler implements CommandHandler<MarkDamagedCommand, InventoryItemResult> {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(MarkDamagedCommand command) {
        InventoryItem item = requireAccessibleActiveItem(command.inventoryItemId(), command.scopeKey(), command.scopeId());
        StockMovementResult result = inventoryStockService.markDamaged(
                item.getId(), command.quantity(), command.notes(), command.createdBy());
        return InventoryItemResults.from(result.item());
    }

    @Override
    public Class<MarkDamagedCommand> getCommandType() {
        return MarkDamagedCommand.class;
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
