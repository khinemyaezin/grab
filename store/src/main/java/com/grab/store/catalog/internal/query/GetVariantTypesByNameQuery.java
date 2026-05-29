package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetVariantTypesByNameQuery(
        String name
) implements Query<VariantTypeResult> {
}
