package com.grab.store.pricing.queries;

import com.grab.framework.cqrs.query.Query;

import java.util.List;

public record ListVariantPriceSetLinksQuery(List<String> variantIds)
        implements Query<List<VariantPriceSetLinkResult>> {
}
