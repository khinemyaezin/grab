package com.grab.store.catalog.query;

import java.util.List;
import java.util.Optional;

public interface CatalogBuyabilityQueryPort {
    Optional<CatalogVariantSlice> findVariant(String variantId);

    boolean isPublished(String variantId, String salesChannelId);

    List<String> variantIdsForProduct(String productId);

    List<PublicationSlice> listPublications();

    List<String> salesChannelIdsForVariant(String variantId);

    record CatalogVariantSlice(
            String variantId,
            String productId,
            String sellerId,
            String sku,
            String title,
            String slug,
            String productStatus,
            boolean untracked,
            String media
    ) {
        public boolean active() {
            return "ACTIVE".equals(productStatus);
        }
    }

    record PublicationSlice(String variantId, String salesChannelId) {
    }
}
