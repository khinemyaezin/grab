package com.grab.store.catalog.internal.query;

import java.util.List;

public record GetProductResult(
        String id,
        String name,
        Category category,
        String condition,
        String status,
        String slug,
        List<Description> descriptions,
        List<Media> medias,
        List<Variant> variants,
        List<VariantType> variantTypes,
        List<Publication> publications
) {
    public GetProductResult(
            String id,
            String name,
            Category category,
            String condition,
            String status,
            String slug,
            List<Description> descriptions,
            List<Media> medias,
            List<Variant> variants,
            List<VariantType> variantTypes
    ) {
        this(id, name, category, condition, status, slug, descriptions, medias, variants, variantTypes, List.of());
    }

    public record Publication(String salesChannelId) {}
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

    public record Category(
            String id,
            String name
    ){}

    public record Variant(
            String id,
            String sku,
            String status,
            String matrixKey,
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
