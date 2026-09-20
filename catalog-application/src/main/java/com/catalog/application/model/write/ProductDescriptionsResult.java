package com.catalog.application.model.write;

import java.util.List;

public record ProductDescriptionsResult(
        String productId,
        List<GetProductPayload.Description> descriptions
) {
}
