package com.pricing.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Map;

public record CalculatePricesQuery(
        List<Id> priceSetIds,
        String currencyCode,
        Integer quantity,
        Map<String, String> attributes
) implements Query<List<CalculatedPriceSetResult>> {
}
