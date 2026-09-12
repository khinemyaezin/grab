package com.grab.store.workflows.internal.workflows.createsellableproduct.rest.dto.response;

import java.time.Instant;
import java.util.List;

public record CreateSellableProductResponse(
        String workflowId,
        String status,
        String currentStep,
        String productId,
        List<PricePair> pricePairs,
        List<String> inventoryItemIds,
        String errorMessage,
        Instant updatedAt
) {

    public record PricePair(String variantId, String sku, String priceSetId) {
    }
}
