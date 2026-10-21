package com.grab.discovery.application.model.projection;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DiscoveryProductProjection(
        Id salesChannelId,
        Id productId,
        Id sellerId,
        String title,
        String slug,
        String description,
        String thumbnail,
        String productStatus,
        boolean published,
        boolean channelEnabled,
        boolean hasStock,
        String currency,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<ProductOption> options,
        List<VariantOffer> variants,
        Instant updatedAt
) {
    public DiscoveryProductProjection {
        options = options == null ? List.of() : List.copyOf(options);
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    public record ProductOption(String name, List<String> values) {
        public ProductOption {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }

    public record VariantOffer(
            Id variantId,
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
        public VariantOffer {
            optionValues = optionValues == null ? Map.of() : Map.copyOf(optionValues);
        }
    }
}
