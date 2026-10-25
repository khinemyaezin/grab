package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

import java.util.List;

public record BulkUpsertProductsResponse(
        List<Entry> results
) {
    public record Entry(
            String productId,
            String operation
    ) {
    }
}
