package com.catalog.application.port.outbound;

import java.util.List;
import java.util.Optional;

public interface BuyabilityQueryPort {

    Optional<VariantSlice> findVariant(String variantId);

    boolean isPublished(String variantId, String salesChannelId);

    List<String> variantIdsForProduct(String productId);

    List<PublicationSlice> listPublications();

    List<String> salesChannelIdsForVariant(String variantId);

    record VariantSlice(
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
