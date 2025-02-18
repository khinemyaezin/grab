package com.pricing.domain.aggregate.price_adjustment;

import com.pricing.domain.aggregate.PricingContext;
import lombok.Getter;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class DiscountAdjustment extends AbstractPricingAdjustment<String> {
    public DiscountAdjustment(Id id,BigDecimal rate) {
        super(id,rate);
    }

    @Override
    public Money apply(Money basePrice, PricingContext context) {
        Money discount = basePrice.multipliedBy(rate, RoundingMode.HALF_UP);
        return basePrice.minus(discount);
    }

}
