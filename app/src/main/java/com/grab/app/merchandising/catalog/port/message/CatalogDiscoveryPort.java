package com.grab.app.merchandising.catalog.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Java Port Interface for Product Discovery and Buyability queries.
 * Unifies legacy CatalogDiscoveryQuery and CatalogBuyabilityQuery.
 */
public interface CatalogDiscoveryPort {

    Optional<ProductFactsResult> findProductFacts(Id productId);

    Optional<VariantFactsResult> findVariantFacts(Id variantId);

    boolean isPublishedInChannel(Id productId, Id salesChannelId);

    Optional<CatalogVariantSliceResult> findPublishedVariantSlice(Id variantId, Id salesChannelId);

    Optional<CatalogVariantSliceResult> findVariant(Id variantId);

    List<Id> variantIdsForProduct(Id productId);

    List<PublicationSliceResult> listPublications();

    List<ProductChannelPairResult> findPublishedProductChannelsAfter(Id productId, Id salesChannelId, int limit);

    List<ProductChannelPairResult> findPublishedProductChannelsBySku(String sku);

    List<ProductChannelPairResult> findPublishedProductChannelsForSalesChannelAfter(Id salesChannelId, Id afterProductId, int limit);

    record ProductFactsResult(
            Id productId,
            Id merchantId,
            Id categoryId,
            String title,
            String slug,
            String description,
            String thumbnail,
            String status,
            List<ProductOptionResult> options,
            List<VariantFactsResult> variants
    ) {
        public ProductFactsResult {
            options = options == null ? List.of() : List.copyOf(options);
            variants = variants == null ? List.of() : List.copyOf(variants);
        }
    }

    record ProductOptionResult(String name, List<String> values) {
        public ProductOptionResult {
            values = values == null ? List.of() : List.copyOf(values);
        }
    }

    record VariantFactsResult(
            Id productId,
            Id variantId,
            String sku,
            String title,
            String status,
            boolean manageInventory,
            Map<String, String> optionValues,
            List<Id> salesChannelIds
    ) {
        public VariantFactsResult {
            optionValues = optionValues == null ? Map.of() : Map.copyOf(optionValues);
            salesChannelIds = salesChannelIds == null ? List.of() : List.copyOf(salesChannelIds);
        }
    }

    record CatalogVariantSliceResult(
            Id variantId,
            Id productId,
            Id merchantId,
            String sku,
            String title,
            String slug,
            String productStatus,
            boolean untracked,
            String media
    ) {
        public boolean active() {
            return "ACTIVE".equalsIgnoreCase(productStatus);
        }
    }

    record PublicationSliceResult(Id variantId, Id salesChannelId) {}

    record ProductChannelPairResult(Id productId, Id salesChannelId) {}
}

