package com.grab.store.catalog.internal.command;

import java.util.List;

public record ProductDescriptionsResult(
        String productId,
        List<GetProductPayload.Description> descriptions
) {
}
