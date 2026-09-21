package com.inventory.application.policy;

import com.inventory.application.model.read.InventoryItemView;

public final class ReorderSuggestionCalculator {
    private ReorderSuggestionCalculator() {
    }

    public enum Priority {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public static Priority priority(InventoryItemView item) {
        int available = available(item);
        int safetyStock = item.safetyStock();
        int reorderPoint = item.reorderPoint();

        if (available <= 0 || available <= safetyStock) {
            return Priority.CRITICAL;
        }
        if (available <= reorderPoint) {
            return Priority.HIGH;
        }
        if (available <= reorderPoint * 1.2) {
            return Priority.MEDIUM;
        }
        return Priority.LOW;
    }

    public static boolean shouldSuggest(InventoryItemView item) {
        return priority(item) != Priority.LOW;
    }

    public static int available(InventoryItemView item) {
        return item.onHand() - item.reserved() - item.damaged();
    }
}
