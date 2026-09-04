package com.grab.store.pricing.internal.command;

public record UpdateVariantPriceResult(
        String priceSetId,
        String priceId,
        boolean priceSetCreated
) {
}
