package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetVariantOptionsByNameQuery(
        String name,
        String typeId
) implements Query<VariantOptionResult> {
}
