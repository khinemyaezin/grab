package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.InventoryItemResults;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdjustStockCommandHandler implements CommandHandler<AdjustStockCommand, InventoryItemResult> {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(AdjustStockCommand command) {
        
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

    @Override
    public Class<AdjustStockCommand> getCommandType() {
        return AdjustStockCommand.class;
    }
}
