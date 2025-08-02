package com.pricing.domain.aggregate.pricing_model;

import com.pricing.domain.aggregate.PricingContext;
import lombok.Getter;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class CostBasedPricing extends AbstractPricingModel<String> {
    private final Money basePrice;
    private final BigDecimal markupPercentage;

    public CostBasedPricing(Id id, Money basePrice, BigDecimal markupPercentage) {
        super(id);
        this.basePrice = basePrice;
        this.markupPercentage = markupPercentage;
    }

    @Override
    public Money calculatePrice(PricingContext context) {
        return basePrice.plus(basePrice.multipliedBy(markupPercentage, RoundingMode.HALF_UP));
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        return false;
    }
}
