package com.grab.discovery.adapter.persistence.entity;

import java.math.BigDecimal;
import java.util.Map;

public record DiscoveryVariantOfferDocument(
        String variantId,
        String sku,
        String title,
        Map<String, String> optionValues,
        BigDecimal price,
        BigDecimal compareAtPrice,
        int availableQty,
        boolean manageInventory,
        boolean allowBackorder,
        boolean inStock,
        boolean buyable
) {
    public DiscoveryVariantOfferDocument {
        optionValues = optionValues == null ? Map.of() : Map.copyOf(optionValues);
    }
}
