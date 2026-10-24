package com.grab.discovery.application.port.outbound;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CatalogPort {
    Optional<ProductFacts> findProduct(Id productId);

    Optional<VariantFacts> findVariant(Id variantId);

    List<ProductChannelPair> findPublishedProductChannelsAfter(Id productId, Id salesChannelId, int limit);

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
            optionValues = optionValues == null ? Map.of() : Map.copyOf(optionValues);
            salesChannelIds = salesChannelIds == null ? List.of() : List.copyOf(salesChannelIds);
        }
    }

    record ProductChannelPair(Id productId, Id salesChannelId) {
    }
}
