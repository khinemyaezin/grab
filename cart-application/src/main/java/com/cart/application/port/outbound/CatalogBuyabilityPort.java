package com.cart.application.port.outbound;

import java.util.Optional;

public interface CatalogBuyabilityPort {
    Optional<BuyableVariant> findPublished(String variantId, String salesChannelId);

    record BuyableVariant(
            String variantId,
            String productId,
            String sellerId,
            String sku,
            String title,
            boolean untracked
    ) {
    }
}
