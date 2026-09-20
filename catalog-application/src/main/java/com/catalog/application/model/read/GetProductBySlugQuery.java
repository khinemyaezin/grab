package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetProductBySlugQuery(
        String slug
) implements Query<GetProductBySlugResult> {}
