package com.grab.discovery.application.model.read;

import com.grab.framework.id.Id;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DiscoveryProductView(
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
        List<VariantFact> variants
) {
    public DiscoveryProductView {
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
            optionValues = optionValues == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(optionValues));
        }
    }
}
