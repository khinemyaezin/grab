package com.grab.store.product.internal.api.rest.dto.response;

import java.io.Serializable;

public record DeleteProductResponse(
        String productId,
        boolean deleted
) implements Serializable {
}
