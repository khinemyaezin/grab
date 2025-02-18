package com.pricing.domain.aggregate;

import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Discount {
    private final BigDecimal percentage;

    public Discount(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public Money calculateDiscount(Money originalPrice) {
        return originalPrice.multipliedBy(percentage.divide(BigDecimal.valueOf(100)), RoundingMode.HALF_UP);
    }
}
