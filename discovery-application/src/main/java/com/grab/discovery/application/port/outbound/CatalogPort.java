package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public interface CatalogPort {
    Optional<CatalogVariant> findVariant(Id variantId);

    List<Id> variantIdsForProduct(Id productId);

    List<Publication> listPublications();

    List<Id> salesChannelIdsForVariant(Id variantId);

    record CatalogVariant(
            Id variantId,
            Id productId,
            Id sellerId,
            String sku,
            String title,
            String slug,
            String productStatus,
            boolean untracked,
            String media
    ) {
    }

    record Publication(Id variantId, Id salesChannelId) {
    }
}
