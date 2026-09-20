package com.catalog.application.model.read;

import com.catalog.domain.valueobject.ProductStatus;

import java.util.List;

public record ProductDetailView(
        String id,
        String name,
        String merchantId,
        ProductStatus status,
        String slug,
        String listingCondition,
        String categoryId,
        boolean featured,
        List<DescriptionView> descriptions,
        List<MediaView> medias,
        List<VariantView> variants
) {
    public boolean visibleOnStorefront() {
        return status == ProductStatus.ACTIVE
                && variants.stream().anyMatch(v -> "ACTIVE".equalsIgnoreCase(v.status()));
    }

    public record DescriptionView(String id, String name, String title, String description) {
    }

    public record MediaView(
            String id,
            String storageKey,
            String url,
            String contentType,
            int rank
    ) {
    }

    public record VariantView(
            String id,
            String sku,
            String status,
            boolean manageInventory,
            List<String> mediaIds,
            String thumbnailMediaId,
            List<VariationView> variations
    ) {
    }

    public record VariationView(String optionId, String typeId) {
    }
}
