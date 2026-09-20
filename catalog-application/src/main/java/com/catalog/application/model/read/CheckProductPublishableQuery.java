package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record CheckProductPublishableQuery(
        String productId,
        String merchantId
) implements Query<CheckProductPublishableResult> {
}
