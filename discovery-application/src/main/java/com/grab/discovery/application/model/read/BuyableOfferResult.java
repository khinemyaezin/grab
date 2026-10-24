package com.grab.discovery.application.model.read;

import com.grab.framework.id.Id;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record BuyableOfferResult(
        Id salesChannelId,
        Id regionId,
        String countryCode,
        String currencyCode,
        String locale,
        boolean regionServed,
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
        boolean buyable,
        String minPrice,
        String maxPrice,
        List<ProductOption> options,
        List<VariantOffer> variants
) {
    public BuyableOfferResult {
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
            String status,
            Map<String, String> optionValues,
            int availableQty,
            boolean manageInventory,
            boolean allowBackorder,
            boolean inStock,
            String availability,
            boolean buyable,
            String amount,
            String originalAmount
    ) {
        public VariantOffer {
            optionValues = optionValues == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(optionValues));
        }
    }
}
