package com.inventory.application.model.write;

import com.grab.framework.id.Id;

public record ProductVariantProjection(
        Id productId, Id variantId, String sku, String productName, boolean manageInventory, String status
) {
}
