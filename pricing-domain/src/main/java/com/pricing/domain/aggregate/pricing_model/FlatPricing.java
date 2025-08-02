package com.pricing.domain.aggregate.pricing_model;

import com.pricing.domain.aggregate.PricingContext;
import lombok.Getter;
import org.joda.money.Money;

/**
 * Flat Pricing
 * 	•	A single price for a product regardless of other factors.
 * 	•	Suitable for simpler use cases or standard retail setups.
 * 	•	Example: $10 for a T-shirt, no discounts or special rules.
 */
public class FlatPricing extends AbstractPricingModel<String> {
    @Getter
    private final Money price;

    public FlatPricing(Id id, Money price) {
        super(id);
        this.price = price;
    }

    @Override
    public Money calculatePrice(PricingContext context) {
        return price;
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        return true; // Always applicable for flat pricing
    }
}
