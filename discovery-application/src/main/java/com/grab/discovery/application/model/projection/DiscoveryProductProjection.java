package com.grab.discovery.application.model.projection;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DiscoveryProductProjection(
        Id salesChannelId,
        Id productId,
        Id merchantId,
        Id categoryId,
        String title,
        String slug,
        String description,
        String thumbnail,
        String productStatus,
        boolean published,
        boolean channelEnabled,
        boolean hasStock,
        List<ProductOption> options,
        List<VariantFact> variants,
        Instant updatedAt,
        long version
) {
    public static final long NEW_PROJECTION_VERSION = -1L;

    public DiscoveryProductProjection {
        options = options == null ? List.of() : List.copyOf(options);
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    public record ProductOption(String name, List<String> values) {
        public ProductOption {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }

    public record VariantFact(
            Id variantId,
            String sku,
            String title,
            String status,
            Map<String, String> optionValues,
            int availableQty,
            boolean manageInventory,
            boolean allowBackorder,
            boolean inStock
    ) {
        public VariantFact {
            optionValues = optionValues == null ? Map.of() : Map.copyOf(optionValues);
        }
    }
}
