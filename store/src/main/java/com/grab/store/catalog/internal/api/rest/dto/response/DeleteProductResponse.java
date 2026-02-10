package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;

public record DeleteProductResponse(
        String productId,
        boolean deleted
) implements Serializable {
}
