package com.grab.store.workflows.events;

import com.inventory.domain.enums.AdjustmentReason;

public final class InventorySyncPayload {

    private InventorySyncPayload() {
    }

    public record CreateStock(
            int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) {
    }

    public record AdjustStock(
            int newOnHandQuantity,
            AdjustmentReason reason
    ) {
    }

    public record DamageStock(
            int quantity,
            String notes
    ) {
    }

    public record WriteOffStock(
            int quantity,
            String reason,
            String notes
    ) {
    }

    public record Reorder(
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) {
    }
}
