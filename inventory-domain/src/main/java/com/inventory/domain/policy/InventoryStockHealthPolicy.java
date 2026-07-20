package com.inventory.domain.policy;

import com.inventory.domain.enums.StockHealth;

public final class InventoryStockHealthPolicy {

    private InventoryStockHealthPolicy() {
    }

    public static StockHealth classify(int available, int safetyStock, int reorderPoint) {
        if (available <= 0) {
            return StockHealth.OUT_OF_STOCK;
        }
        if (available <= safetyStock) {
            return StockHealth.CRITICAL;
        }
        if (available <= reorderPoint) {
            return StockHealth.LOW_STOCK;
        }
        return StockHealth.IN_STOCK;
    }
}
