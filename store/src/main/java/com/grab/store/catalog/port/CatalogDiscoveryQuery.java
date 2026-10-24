package com.grab.store.catalog.port;

import com.grab.framework.id.Id;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CatalogDiscoveryQuery {
    Optional<ProductFacts> findProduct(Id productId);

    Optional<VariantFacts> findVariant(Id variantId);

    List<ProductChannelPair> findPublishedProductChannelsAfter(Id productId, Id salesChannelId, int limit);

    List<ProductChannelPair> findPublishedProductChannelsBySku(String sku);

    List<ProductChannelPair> findPublishedProductChannelsForSalesChannelAfter(
            Id salesChannelId,
            Id afterProductId,
            int limit
    );

    boolean isPublishedInChannel(Id productId, Id salesChannelId);

    record ProductFacts(
            Id productId,
            Id merchantId,
            Id categoryId,
            String title,
            String slug,
            String description,
            String thumbnail,
            String status,
            List<ProductOption> options,
            List<VariantFacts> variants
    ) {
        public ProductFacts {
            options = options == null ? List.of() : List.copyOf(options);
            variants = variants == null ? List.of() : List.copyOf(variants);
        }
    }

    record ProductOption(String name, List<String> values) {
        public ProductOption {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }

    record VariantFacts(
            Id productId,
            Id variantId,
            String sku,
            String title,
            String status,
            boolean manageInventory,
            Map<String, String> optionValues,
            List<Id> salesChannelIds
    ) {
        public VariantFacts {
            optionValues = optionValues == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(optionValues));
            salesChannelIds = salesChannelIds == null ? List.of() : List.copyOf(salesChannelIds);
        }
    }

    record ProductChannelPair(Id productId, Id salesChannelId) {
    }
}
