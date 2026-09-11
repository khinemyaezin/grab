package com.grab.store.workflows.internal.updateproductvariant.rest.dto.response;

public record UpdateProductVariantResponse(
        String workflowId,
        String status,
        String currentStep,
        String productId,
        String variantId,
        String sku,
        PricePair pricePair,
        String inventoryItemId,
        String errorMessage
) {

    public record PricePair(String variantId, String sku, String priceSetId) {
    }
}
