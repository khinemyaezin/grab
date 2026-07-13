package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.InventoryStockService;
import com.inventory.domain.service.InventoryStockService.StockMovementResult;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.ReceiveStockCommand;
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
public class ReceiveStockCommandHandler implements CommandHandler<ReceiveStockCommand, InventoryItemResult> {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;
    private final InventoryStockService inventoryStockService;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(ReceiveStockCommand command) {
        
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

        return mapToInventoryItemResult(result.item());
    }

    @Override
    public Class<ReceiveStockCommand> getCommandType() {
        return ReceiveStockCommand.class;
    }

    private InventoryItemResult mapToInventoryItemResult(InventoryItem item) {
        return new InventoryItemResult(
                item.getId().getValue(),
                item.getSku(),
                item.getMerchantId() == null ? null : item.getMerchantId().getValue(),
                item.getProductVariantId() == null ? null : item.getProductVariantId().getValue(),
                item.getLocationId().getValue(),
                item.getQuantity().onHand(),
                item.getQuantity().reserved(),
                item.getQuantity().damaged(),
                item.getAvailableQuantity(),
                item.getStatus().name(),
                item.getReorderConfig().safetyStock(),
                item.getReorderConfig().reorderPoint(),
                item.getReorderConfig().reorderQuantity(),
                item.getReorderConfig().maxStock()
        );
    }
}
