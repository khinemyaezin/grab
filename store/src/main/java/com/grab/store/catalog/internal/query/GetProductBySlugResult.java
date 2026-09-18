package com.grab.store.catalog.internal.query;

import java.util.List;

public record GetProductBySlugResult(
        String id,
        String name,
        String categoryId,
        String merchantId,
        String merchantType,
        String condition,
        String status,
        String slug,
        boolean featured,
        List<Description> descriptions,
        List<Media> medias,
        List<Variant> variants,
        List<VariantType> variantTypes
) {
    public record Description(
            String id,
            String name,
            String title,
            String description
    ) {}

    public record Media(
            String id,
            String storageKey,
            String url,
            String contentType,
            int rank
    ) {}

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations,
            boolean manageInventory,
            List<String> mediaIds,
            String thumbnailMediaId
    ) {}

    public record Variation(
            String optionId,
            String optionName,
            String typeId,
            String typeName
    ) {}

    public record VariantType(
            String typeId,
            String typeName,
            List<VariantOption> options
    ){}

    public record VariantOption(
            String optionId,
            String optionName
    ) {}
}
