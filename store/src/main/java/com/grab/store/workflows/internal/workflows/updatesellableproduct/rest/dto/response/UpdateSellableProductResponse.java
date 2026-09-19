package com.grab.store.workflows.internal.workflows.updatesellableproduct.rest.dto.response;

import java.util.List;

public record UpdateSellableProductResponse(
        String workflowId,
        String status,
        String currentStep,
        String productId,
        boolean productUpdated,
        List<PricePair> pricePairs,
        List<String> inventoryItemIds,
        int compensatedPriceSetCount,
        List<PublicationPair> writtenPublications,
        List<String> missingRouteChannelIds,
        boolean partiallyApplied,
        String errorMessage
) {

    public record PricePair(String variantId, String sku, String priceSetId) {
    }

    public record PublicationPair(String variantId, String sku, String salesChannelId) {
    }
}
