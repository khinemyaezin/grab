package com.grab.store.pricing.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.grab.store.pricing.internal.command.PriceSetResult;

public record GetPriceSetQuery(Id priceSetId) implements Query<PriceSetResult> {
}
