package com.grab.store.inventory.internal.command;

import com.inventory.domain.aggregate.InventoryItem;

public final class InventoryItemResults {

    private InventoryItemResults() {
    }

    public static InventoryItemResult from(InventoryItem item) {
        return new InventoryItemResult(
                item.getId().getValue(),
                item.getSku(),
                item.getMerchantId() == null ? null : item.getMerchantId().getValue(),
                item.getProductVariantId() == null ? null : item.getProductVariantId().getValue(),
                item.getLocationId() == null ? null : item.getLocationId().getValue(),
                item.getQuantity().onHand(),
                item.getQuantity().reserved(),
                item.getQuantity().inTransit(),
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
