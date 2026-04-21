package com.grab.store.catalog.internal.query;

import java.util.List;

public record GetProductBySlugResult(
        String id,
        String name,
        String categoryId,
        String condition,
        String status,
        String slug,
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
            String type,
            String path
    ) {}

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations
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
