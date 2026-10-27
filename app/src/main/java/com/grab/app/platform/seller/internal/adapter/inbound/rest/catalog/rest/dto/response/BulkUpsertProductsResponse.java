package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

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
