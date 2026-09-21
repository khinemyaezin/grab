package com.pricing.application.model.write;

public record UpdateVariantPriceResult(
        String priceSetId,
        String priceId,
        boolean priceSetCreated
) {
}
