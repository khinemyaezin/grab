package com.pricing.domain.aggregate.pricing_model;

import com.pricing.domain.aggregate.PricingContext;
import org.joda.money.Money;

public class DynamicPricing extends AbstractPricingModel<String> {
    protected DynamicPricing(String s) {
        super(s);
    }

    @Override
    public Money calculatePrice(PricingContext context) {
        return null;
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        return false;
    }
}
