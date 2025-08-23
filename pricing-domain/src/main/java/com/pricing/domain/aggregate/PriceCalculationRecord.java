package com.pricing.domain.aggregate;

import org.joda.money.Money;

import java.time.LocalDateTime;

public record PriceCalculationRecord(
        PricingContext context,
        Money finalPrice,
        LocalDateTime timestamp
) {

}
