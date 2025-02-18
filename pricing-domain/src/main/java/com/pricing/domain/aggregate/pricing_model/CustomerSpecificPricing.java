package com.pricing.domain.aggregate.pricing_model;

import com.pricing.domain.aggregate.PricingContext;
import org.joda.money.Money;

import java.util.Map;

/**
 * 	Customer-Specific Pricing
 * 	•	Personalized pricing based on customer profiles, memberships, or loyalty levels.
 * 	•	Example: Gold members get a 15% discount.
 */
public class CustomerSpecificPricing extends AbstractPricingModel<String> {
    private Map<String, Money> customerPricing; // Key: Customer ID, Value: Price

    public CustomerSpecificPricing(Id id, Map<String, Money> customerPricing) {
        super(id);
        this.customerPricing = customerPricing;

    }

    @Override
    public Money calculatePrice(PricingContext context) {
        return customerPricing.getOrDefault(context.customerId(), Money.zero(context.currency()));
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        return true;
    }
}
