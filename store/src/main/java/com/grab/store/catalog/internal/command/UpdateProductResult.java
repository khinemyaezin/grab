package com.grab.store.catalog.internal.command;

import java.util.List;

public record UpdateProductResult(
        String productId,
        String name,
        String categoryId,
        String condition,
        String status,
        String slug,
        List<GetProductPayload.Description> descriptions,
        List<GetProductPayload.Media> medias
) {}
