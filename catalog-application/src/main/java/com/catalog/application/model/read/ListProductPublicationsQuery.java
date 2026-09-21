package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record ListProductPublicationsQuery(
        String merchantId,
        String productId
) implements Query<List<ProductPublicationItem>> {
}
