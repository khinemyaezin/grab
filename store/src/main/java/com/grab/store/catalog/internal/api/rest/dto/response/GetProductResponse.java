package com.grab.store.catalog.internal.api.rest.dto.response;

import java.util.List;

public record GetProductResponse(
        String id,
        String name,
        Category category,
        String sellerId,
        String sellerType,
        String condition,
        boolean offerEligible,
        String status,
        String slug,
        boolean featured,
        List<Description> descriptions,
        List<Media> medias,
        String moderationNote,
        List<Variant> variants,
        List<VariantType> variantTypes
) {
    public record Publication(String salesChannelId) {}
    public record Category(
            String id,
            String name
    ){}
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
            String matrixKey,
            List<Variation> variations,
            boolean manageInventory,
            List<String> mediaIds,
            String thumbnailMediaId,
            List<Publication> publications
    ) {
        public Variant {
            publications = publications == null ? List.of() : List.copyOf(publications);
        }
    }

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
    ) {}

    public record VariantOption(
            String optionId,
            String optionName
    ) {}
}
