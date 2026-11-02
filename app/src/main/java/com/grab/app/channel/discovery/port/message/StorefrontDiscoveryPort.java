package com.grab.app.channel.discovery.port.message;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Java Port Interface for Contextual Storefront Discovery and Product Search.
 * Replaces StorefrontProductController and StorefrontQueryRootController.
 */
public interface StorefrontDiscoveryPort {

    StorefrontSearchResult searchProducts(StorefrontSearchCriteria criteria);

    Optional<StorefrontProductDetailResult> findProductBySlug(String slug, ShoppingContext context);

    ShoppingContext resolveShoppingContext(String rawPublishableKey, String countryOrRegionId);

    record ShoppingContext(
            Id salesChannelId,
            Id regionId,
            String currencyCode,
            String countryCode
    ) {}

    record StorefrontSearchCriteria(
            ShoppingContext context,
            Id categoryId,
            String query,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Map<String, String> attributeFilters,
            int page,
            int size
    ) {}

    record StorefrontSearchResult(
            List<StorefrontProductSummaryResult> items,
            long totalElements,
            int totalPages,
            int currentPage
    ) {}

    record StorefrontProductSummaryResult(
            Id productId,
            String title,
            String slug,
            String thumbnail,
            BigDecimal price,
            BigDecimal originalPrice,
            String currencyCode,
            boolean inStock
    ) {}

    record StorefrontProductDetailResult(
            Id productId,
            String title,
            String slug,
            String description,
            List<String> mediaUrls,
            List<StorefrontVariantDetailResult> variants
    ) {}

    record StorefrontVariantDetailResult(
            Id variantId,
            String sku,
            String title,
            BigDecimal price,
            BigDecimal originalPrice,
            String currencyCode,
            boolean inStock,
            Map<String, String> optionValues
    ) {}
}

