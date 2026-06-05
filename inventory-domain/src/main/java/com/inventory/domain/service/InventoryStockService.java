package com.inventory.domain.service;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.enums.AdjustmentReason;
import com.inventory.domain.enums.StockMovementType;

public interface InventoryStockService {

    StockMovementResult receiveStock(Id inventoryItemId, int quantity, StockMovementType type,
                                     String referenceId, String notes, Id userId);

    StockMovementResult adjustStock(Id inventoryItemId, int newOnHandQuantity, AdjustmentReason reason,
                                    String notes, Id userId);

    record StockMovementResult(InventoryItem item, StockMovement movement) {
    }
}
