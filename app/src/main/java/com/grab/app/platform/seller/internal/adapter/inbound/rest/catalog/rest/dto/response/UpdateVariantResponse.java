package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.io.Serializable;

public record UpdateVariantResponse(
        String productId,
        String variantId,
        String sku,
        String status
) implements Serializable {
}
