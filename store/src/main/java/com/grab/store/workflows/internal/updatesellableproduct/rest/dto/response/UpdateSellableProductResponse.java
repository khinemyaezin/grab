package com.grab.store.workflows.internal.updatesellableproduct.rest.dto.response;

import java.util.List;

public record UpdateSellableProductResponse(
        String workflowId,
        String status,
        String currentStep,
        String productId,
        List<PricePair> pricePairs,
        List<String> inventoryItemIds,
        String errorMessage
) {

    public record PricePair(String variantId, String sku, String priceSetId) {
    }
}
