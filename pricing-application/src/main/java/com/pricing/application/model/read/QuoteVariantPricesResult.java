package com.pricing.application.model.read;

import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record QuoteVariantPricesResult(Map<Id, VariantQuote> quotes) {
    public QuoteVariantPricesResult {
        Map<Id, VariantQuote> copied = quotes == null ? Map.of() : new LinkedHashMap<>(quotes);
        quotes = Collections.unmodifiableMap(copied);
    }

    public record VariantQuote(
            BigDecimal amount,
            BigDecimal originalAmount,
            String currencyCode,
            Id priceSetId
    ) {
    }
}
