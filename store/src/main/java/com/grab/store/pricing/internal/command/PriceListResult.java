package com.grab.store.pricing.internal.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PriceListResult(
        String id,
        String title,
        String description,
        String status,
        String type,
        Instant startsAt,
        Instant endsAt,
        List<PriceListRuleResult> rules,
        List<PriceResult> prices
) {
    public record PriceListRuleResult(String id, String attribute, List<String> values) {
    }

    public record PriceResult(
            String id,
            String title,
            String currencyCode,
            BigDecimal amount,
            Integer minQuantity,
            Integer maxQuantity,
            String priceSetId,
            String priceListId,
            List<PriceSetResult.PriceRuleResult> rules
    ) {
    }
}
