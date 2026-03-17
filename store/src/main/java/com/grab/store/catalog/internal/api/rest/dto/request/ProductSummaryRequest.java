package com.grab.store.catalog.internal.api.rest.dto.request;

import java.util.List;

public record ProductSummaryRequest(
        String productName,
        String sku,
        String variantStatus,
        String categoryId,
        String sellerId,
        String sellerType,
        Boolean offerEligible,
        List<String> variations,
        int page,
        int size
) {
}
