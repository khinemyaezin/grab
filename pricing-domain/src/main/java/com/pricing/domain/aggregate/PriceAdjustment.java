package com.pricing.domain.aggregate;

import org.joda.money.Money;

public interface PriceAdjustment {
    Money apply(Money basePrice, PricingContext context);
}
