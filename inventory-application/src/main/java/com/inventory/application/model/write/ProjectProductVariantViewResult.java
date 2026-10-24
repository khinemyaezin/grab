package com.inventory.application.model.write;

import com.grab.framework.id.Id;

import java.util.List;

public record ProjectProductVariantViewResult(List<VariantProjected> notifications) {
    public ProjectProductVariantViewResult {
        notifications = List.copyOf(notifications);
    }

    public record VariantProjected(Id productId, Id variantId, String sku) {
    }
}
