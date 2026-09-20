package com.pricing.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import com.pricing.application.model.write.PriceSetResult;

public record GetPriceSetQuery(Id priceSetId) implements Query<PriceSetResult> {
}
