package com.grab.service.contract.event.merchandising.inventory;

import com.grab.framework.id.Id;

import java.util.List;

public final class InventoryWorkflowPayloads {

    private InventoryWorkflowPayloads() {
    }

    public enum Operation { CREATE, ADJUST, DAMAGE, WRITE_OFF, REORDER }

    public record InventoryBatch(Id merchantId, Id actorId, List<Line> lines, List<Variant> variants) {
        public InventoryBatch {
            lines = List.copyOf(lines);
            variants = List.copyOf(variants);
        }
    }

    public record InventoryWritten(List<Id> itemIds) {
        public InventoryWritten {
            itemIds = List.copyOf(itemIds);
        }
    }

    public record Variant(Id variantId, String sku) {
    }

    public record Line(
            Operation operation,
            Id inventoryItemId,
            Id variantId,
            String sku,
            Id locationId,
            Integer quantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock,
            String reason,
            String notes,
            String scopeKey,
            String scopeId
    ) {
    }

    public record StockPaths(Id merchantId, List<Id> salesChannelIds) {
        public StockPaths {
            salesChannelIds = List.copyOf(salesChannelIds);
        }
    }
}
