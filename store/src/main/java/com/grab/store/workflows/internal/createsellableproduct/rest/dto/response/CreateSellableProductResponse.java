package com.grab.store.workflows.internal.createsellableproduct.rest.dto.response;

import java.util.List;

public record CreateSellableProductResponse(
        String workflowId,
        String status,
        String currentStep,
        String productId,
        List<String> inventoryItemIds,
        String errorMessage
) {
}
