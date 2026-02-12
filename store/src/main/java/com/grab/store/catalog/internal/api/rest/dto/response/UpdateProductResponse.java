package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;

public record UpdateProductResponse(
        String productId,
        String name,
        String categoryId,
        String status,
        String slug,
        boolean featured
) implements Serializable {
}
