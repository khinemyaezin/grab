package com.catalog.application.command;

import java.util.List;

public record CreateProductSetResult(
        String productId,
        List<VariantRef> variants,
        String status
) {

    public CreateProductSetResult {
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    public CreateProductSetResult(String productId, List<VariantRef> variants) {
        this(productId, variants, null);
    }

    public record VariantRef(String variantId, String sku) {
    }
}
