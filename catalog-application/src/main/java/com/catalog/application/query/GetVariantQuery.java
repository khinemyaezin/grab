package com.catalog.application.query;

import com.grab.framework.cqrs.query.Query;

public record GetVariantQuery(
        String merchantId,
        String productId,
        String variantId
) implements Query<GetVariantResult> {
}
