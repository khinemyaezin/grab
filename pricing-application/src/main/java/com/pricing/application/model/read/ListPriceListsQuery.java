package com.pricing.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.pricing.application.model.write.PriceListResult;

import java.util.List;

public record ListPriceListsQuery() implements Query<List<PriceListResult>> {
}
