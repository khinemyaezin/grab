package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;

public record UpdateVariantResponse(
        String productId,
        String variantId,
        String sku,
        String status
) implements Serializable {
}
