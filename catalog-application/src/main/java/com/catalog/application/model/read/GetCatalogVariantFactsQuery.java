package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetCatalogVariantFactsQuery(String variantId) implements Query<GetCatalogProductFactsResult> {
}
