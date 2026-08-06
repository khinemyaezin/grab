package com.grab.store.catalog.internal.command;

import java.util.List;

public record CreateProductSetResult(
        String productId,
        List<VariantRef> variants
) {

    public CreateProductSetResult {
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    public record VariantRef(String variantId, String sku) {
    }
}
