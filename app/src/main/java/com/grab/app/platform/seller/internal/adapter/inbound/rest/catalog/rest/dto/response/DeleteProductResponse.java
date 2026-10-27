package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.io.Serializable;

public record DeleteProductResponse(
        String productId,
        boolean deleted
) implements Serializable {
}
