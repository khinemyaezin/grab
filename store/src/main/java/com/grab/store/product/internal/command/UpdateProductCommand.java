package com.grab.store.product.internal.command;

import com.grab.framework.id.Id;

import java.util.List;

public record UpdateProductCommand(
        Id id,
        String name,
        String categoryId,
        List<VariantCommand> variants) {
    public record VariantCommand(
            Id id,
            String sku,
            List<VariationCommand> variations
    ) {
        public VariantCommand(String sku, List<VariationCommand> variations) {
            this(EmptyId.getEmptyId(), sku, variations);
        }
    }
}
