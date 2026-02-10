package com.grab.store.product.internal.api.rest.dto.request;

import java.util.List;

public record ProductSummaryRequest(
        String productName,
        String sku,
        String variantStatus,
        List<String> variations,
        int page,
        int size
) {
}