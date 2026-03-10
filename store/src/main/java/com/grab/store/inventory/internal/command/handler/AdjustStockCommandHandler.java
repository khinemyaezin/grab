package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdjustStockCommandHandler implements CommandHandler<AdjustStockCommand, InventoryItemResult> {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(AdjustStockCommand command) {
        InventoryItem item = inventoryRepository.findById(command.inventoryItemId())
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.InventoryNotFound(command.inventoryItemId().getValue())));

        StockMovement movement = item.adjustStock(
                command.newOnHandQuantity(),
                command.reason(),
                null,
                command.createdBy(),
                idGenerator.generateId()
        );

        inventoryRepository.save(item);
        stockMovementRepository.save(movement);
        return mapToInventoryItemResult(item);
    }

    @Override
    public Class<AdjustStockCommand> getCommandType() {
        return AdjustStockCommand.class;
    }

    private InventoryItemResult mapToInventoryItemResult(InventoryItem item) {
        return new InventoryItemResult(
                item.getId().getValue(),
                item.getSku(),
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
