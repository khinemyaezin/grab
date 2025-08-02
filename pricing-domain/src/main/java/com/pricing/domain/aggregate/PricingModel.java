package com.pricing.domain.aggregate;

import org.joda.money.Money;

public interface PricingModel {
    Money calculatePrice(PricingContext context);
    boolean isApplicable(PricingContext context);
}
