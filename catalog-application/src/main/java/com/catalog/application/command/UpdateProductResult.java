package com.catalog.application.command;

import java.util.List;

public record UpdateProductResult(
        String productId,
        String name,
        String categoryId,
        String condition,
        String status,
        String slug,
        List<GetProductPayload.Description> descriptions,
        List<GetProductPayload.Media> medias,
        List<VariantRef> variants
) {

    public UpdateProductResult {
        descriptions = descriptions == null ? List.of() : List.copyOf(descriptions);
        medias = medias == null ? List.of() : List.copyOf(medias);
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    public record VariantRef(String variantId, String sku) {
    }
}
