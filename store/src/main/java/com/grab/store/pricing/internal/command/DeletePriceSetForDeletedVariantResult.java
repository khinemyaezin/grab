package com.grab.store.pricing.internal.command;

public record DeletePriceSetForDeletedVariantResult(
        String variantId,
        String priceSetId,
        boolean deleted
) {
}
