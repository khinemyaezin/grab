package com.pricing.application.model.write;

public record DeletePriceSetForDeletedVariantResult(
        String variantId,
        String priceSetId,
        boolean deleted
) {
}
