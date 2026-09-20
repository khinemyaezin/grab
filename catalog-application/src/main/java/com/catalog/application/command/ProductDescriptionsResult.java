package com.catalog.application.command;

import java.util.List;

public record ProductDescriptionsResult(
        String productId,
        List<GetProductPayload.Description> descriptions
) {
}
