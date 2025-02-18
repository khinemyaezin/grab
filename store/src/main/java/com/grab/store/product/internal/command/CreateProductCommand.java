package com.grab.store.product.internal.command;

import com.grab.framework.domain.Entity;
import com.grab.framework.id.Id;

import java.util.List;

public record CreateProductCommand(
        Id id,
        String name,
        Id categoryId,
        List<VariantCommand> variants) {
    public record VariantCommand(
            Id id,
            String sku,
            List<VariationCommand> variations
    )  {
    }
}
