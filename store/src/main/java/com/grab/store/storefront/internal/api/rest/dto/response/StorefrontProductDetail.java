package com.grab.store.storefront.internal.api.rest.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record StorefrontProductDetail(
        String id,
        String name,
        String categoryId,
        String sellerId,
        String sellerType,
        String condition,
        boolean offerEligible,
        String status,
        String slug,
        boolean featured,
        List<Description> descriptions,
        List<Media> medias,
        List<Variant> variants,
        List<VariantType> variantTypes
) {
    public record Description(
            String id,
            String name,
            String title,
            String description
    ) {
    }

    public record Media(
            String id,
            String storageKey,
            String url,
            String contentType,
            int rank
    ) {
    }

    public record Variant(
            String id,
            String sku,
            String status,
            List<Variation> variations,
            Price price,
            Stock stock
    ) {
    }

    public record Variation(
            String optionId,
            String optionName,
            String typeId,
            String typeName
    ) {
    }

    public record VariantType(
            String typeId,
            String typeName,
            List<VariantOption> options
    ) {
    }

    public record VariantOption(
            String optionId,
            String optionName
    ) {
    }

    public record Price(
            BigDecimal amount,
            BigDecimal originalAmount,
            String currencyCode
    ) {
    }

    public record Stock(
            int availableQuantity,
            boolean inStock
    ) {
    }
}
