package com.pricing.domain.aggregate.pricing_model;

import com.pricing.domain.aggregate.PricingContext;
import lombok.Getter;
import org.joda.money.Money;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Tiered Pricing
 * 	•	Prices vary based on quantity purchased.
 * 	•	Example: $10 each for 1-10 units, $9 each for 11-50 units.
 */
@Getter
public class TieredPricing extends AbstractPricingModel<String> {
    private final Map<Integer, Money> tierPrices;
    private final LocalDateTime validFrom;
    private final LocalDateTime validTo;

    protected TieredPricing(String s, Map<Integer, Money> tierPrices, LocalDateTime validFrom, LocalDateTime validTo) {
        super(s);
        this.tierPrices = tierPrices;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public Map<Integer, Money> getTierPrices() {
        return Collections.unmodifiableMap(this.tierPrices);
    }

    @Override
    public Money calculatePrice(PricingContext context) {
        int quantity = context.quantity();
        Money price = Money.zero(context.currency());

        for (Map.Entry<Integer, Money> tier : this.tierPrices.entrySet()) {
            if (quantity >= tier.getKey()) {
                price = tier.getValue();
            }
        }
        return price.multipliedBy(quantity);
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        LocalDateTime now = context.effectiveDate();
        return now.isAfter(this.validFrom) && now.isBefore(this.validTo);
    }
}
