package com.pricing.domain.aggregate.price_adjustment;

import com.grab.framework.domain.Entity;
import com.pricing.domain.aggregate.PriceAdjustment;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
public abstract class AbstractPricingAdjustment<ID extends Serializable> extends Entity<ID> implements PriceAdjustment {
    protected final BigDecimal rate;

    protected AbstractPricingAdjustment(ID id, BigDecimal rate) {
        super(id);
        this.rate = rate;
    }
}
