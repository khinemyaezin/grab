package com.grab.store.catalog.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetVariantOptionsByNameQuery(
        String name,
        String typeId
) implements Query<VariantOptionResult> {
}
