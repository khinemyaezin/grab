package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetVariantTypesByNameQuery(
        String name
) implements Query<VariantTypeResult> {
}
