package com.grab.app.channel.discovery.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Java Port Interface for Contextual Storefront Discovery and Product Search.
 * Replaces StorefrontProductController and StorefrontQueryRootController.
 */
public interface StorefrontDiscoveryPort {

    List<StorefrontOfferMessageResult> searchProducts(StorefrontSearchCriteria criteria);

    Optional<StorefrontOfferMessageResult> findProductBySlug(String publishableKey, String countryCode, String locale, String slug);

    ResolvedContextMessageResult resolveShoppingContext(String publishableKey, String countryCode, String locale);

    record StorefrontSearchCriteria(
            String publishableKey,
            String countryCode,
            String locale,
            String keyword,
            Id categoryId,
            int page,
            int size
    ) {}

    record ResolvedContextMessageResult(
            Id salesChannelId,
            Id regionId,
            String countryCode,
            String currencyCode,
            String locale,
            boolean regionServed
    ) {}

    record StorefrontOfferMessageResult(
            Id productId,
            Id merchantId,
            Id categoryId,
            String title,
            String slug,
            String description,
            String thumbnail,
            String productStatus,
            boolean published,
            boolean hasStock,
            boolean buyable,
            String minPrice,
            String maxPrice,
            List<VariantOfferMessageResult> variants
    ) {}

    record VariantOfferMessageResult(
            Id variantId,
            String sku,
            String title,
            String status,
            Map<String, String> optionValues,
            int availableQty,
            boolean inStock,
            boolean buyable,
            String amount,
            String originalAmount
    ) {}
}
