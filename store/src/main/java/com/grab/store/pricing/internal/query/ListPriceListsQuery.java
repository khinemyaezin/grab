package com.grab.store.pricing.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.pricing.internal.command.PriceListResult;

import java.util.List;

public record ListPriceListsQuery() implements Query<List<PriceListResult>> {
}
