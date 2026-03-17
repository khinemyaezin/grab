package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;
import java.util.List;

public record ProductDescriptionsResponse(
        String productId,
        List<GetProductResponse.Description> descriptions
) implements Serializable {
}
