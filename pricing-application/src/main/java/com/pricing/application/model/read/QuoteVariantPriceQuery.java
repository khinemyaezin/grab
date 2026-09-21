package com.pricing.application.model.read;

public record QuoteVariantPriceQuery(
        String variantId,
        String currencyCode,
        int quantity,
        String salesChannelId
) {
}
