package com.pricing.domain.aggregate;

import org.joda.money.CurrencyUnit;

import java.time.LocalDateTime;

public record PricingContext(
        int quantity,
        String customerId,
        LocalDateTime effectiveDate,
        CurrencyUnit currency,
        boolean includeTaxes) {
}
