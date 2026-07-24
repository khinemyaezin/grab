package com.grab.store.pricing.internal.command;

import java.math.BigDecimal;
import java.util.List;

public record PriceSetResult(
        String id,
        List<PriceResult> prices
) {
    public record PriceResult(
            String id,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            String priceSetId,
            String priceListId,
            List<PriceRuleResult> rules
    ) {
    }

    public record PriceRuleResult(
            String id,
            String attribute,
            String value,
            String operator,
            int priority
    ) {
    }
}
