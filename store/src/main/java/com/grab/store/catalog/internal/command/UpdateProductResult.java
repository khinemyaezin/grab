package com.grab.store.catalog.internal.command;

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
        List<VariantRef> variants,
        List<String> addedSkus
) {

    public UpdateProductResult {
        descriptions = descriptions == null ? List.of() : List.copyOf(descriptions);
        medias = medias == null ? List.of() : List.copyOf(medias);
        variants = variants == null ? List.of() : List.copyOf(variants);
        addedSkus = addedSkus == null ? List.of() : List.copyOf(addedSkus);
    }

    public record VariantRef(String variantId, String sku) {
    }
}
