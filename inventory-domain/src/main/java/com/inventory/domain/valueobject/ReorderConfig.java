package com.inventory.domain.valueobject;

public record ReorderConfig(
        int safetyStock,
        int reorderPoint,
        int reorderQuantity,
        Integer maxStock
) {

    public ReorderConfig {
        if (safetyStock < 0) throw new IllegalArgumentException("safetyStock cannot be negative");
        if (reorderPoint < 0) throw new IllegalArgumentException("reorderPoint cannot be negative");
        if (reorderQuantity < 0) throw new IllegalArgumentException("reorderQuantity cannot be negative");
        if (maxStock != null && maxStock < 0) throw new IllegalArgumentException("maxStock cannot be negative");
        if (reorderPoint < safetyStock) {
            throw new IllegalArgumentException("reorderPoint should be >= safetyStock");
        }
    }

    public static ReorderConfig defaultConfig() {
        return new ReorderConfig(0, 0, 0, null);
    }

    public static ReorderConfig of(int safetyStock, int reorderPoint, int reorderQuantity) {
        return new ReorderConfig(safetyStock, reorderPoint, reorderQuantity, null);
    }

    public boolean isLowStock(int availableQuantity) {
        return availableQuantity <= safetyStock;
    }

    public boolean needsReorder(int availableQuantity) {
        return availableQuantity <= reorderPoint;
    }

    public boolean wouldExceedMaxStock(int currentOnHand, int incomingQuantity) {
        if (maxStock == null) return false;
        return (currentOnHand + incomingQuantity) > maxStock;
    }

    public int suggestedOrderQuantity(int currentAvailable) {
        if (!needsReorder(currentAvailable)) return 0;
        if (maxStock != null) {
            return Math.min(reorderQuantity, maxStock - currentAvailable);
        }
        return reorderQuantity;
    }
}
