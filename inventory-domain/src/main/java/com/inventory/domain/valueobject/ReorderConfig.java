package com.inventory.domain.valueobject;

import com.inventory.domain.exception.InventoryDomainError;
import com.inventory.domain.exception.InventoryDomainValidationException;

public record ReorderConfig(
        int safetyStock,
        int reorderPoint,
        int reorderQuantity,
        Integer maxStock
) {

    public ReorderConfig {
        if (safetyStock < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidSafetyStock(safetyStock),
                    "safetyStock cannot be negative"
            );
        }
        if (reorderPoint < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidReorderPoint(reorderPoint),
                    "reorderPoint cannot be negative"
            );
        }
        if (reorderQuantity < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidReorderQuantity(reorderQuantity),
                    "reorderQuantity cannot be negative"
            );
        }
        if (maxStock != null && maxStock < 0) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidMaxStock(maxStock),
                    "maxStock cannot be negative"
            );
        }
        if (reorderPoint < safetyStock) {
            throw new InventoryDomainValidationException(
                    new InventoryDomainError.InvalidReorderConfig(safetyStock, reorderPoint),
                    "reorderPoint should be >= safetyStock"
            );
        }
    }

    public static ReorderConfig defaultConfig() {
        return new ReorderConfig(0, 0, 0, null);
    }

    public static ReorderConfig of(int safetyStock, int reorderPoint, int reorderQuantity) {
        return new ReorderConfig(safetyStock, reorderPoint, reorderQuantity, null);
    }

    public boolean isLowStock(int availableQuantity) {
        if(safetyStock == 0) return false;
        return availableQuantity <= safetyStock;
    }

    public boolean needsReorder(int availableQuantity) {
        return availableQuantity <= reorderPoint;
    }

    public boolean wouldExceedMaxStock(int currentOnHand, int incomingQuantity) {
        if (maxStock == null) {
            return false;
        }
        return (currentOnHand + incomingQuantity) > maxStock;
    }

    public int suggestedOrderQuantity(int currentAvailable) {
        if (!needsReorder(currentAvailable)) {
            return 0;
        }
        if (maxStock == null) {
            return reorderQuantity;
        }
        return Math.max(0, Math.min(reorderQuantity, maxStock - currentAvailable));
    }
}
