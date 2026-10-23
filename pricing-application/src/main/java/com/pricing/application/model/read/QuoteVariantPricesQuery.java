package com.pricing.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record QuoteVariantPricesQuery(
        List<Id> variantIds,
        String currencyCode,
        int quantity,
        Id salesChannelId,
        Id regionId
) implements Query<QuoteVariantPricesResult> {
    public QuoteVariantPricesQuery {
        variantIds = variantIds == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(variantIds));
    }
}
