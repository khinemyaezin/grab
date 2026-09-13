package com.grab.store.workflows.internal.workflows.updateproductvariant.rest.dto.response;

import java.util.List;

public record UpdateProductVariantResponse(
        String workflowId,
        String status,
        String currentStep,
        String productId,
        String variantId,
        String sku,
        boolean variantUpdated,
        PricePair pricePair,
        List<String> inventoryItemIds,
        int compensatedPriceSetCount,
        boolean partiallyApplied,
        String errorMessage
) {

    public record PricePair(String variantId, String sku, String priceSetId) {
    }
}
