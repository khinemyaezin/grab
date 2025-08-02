package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;
import com.pricing.domain.aggregate.price_adjustment.DiscountAdjustment;
import com.pricing.domain.aggregate.price_adjustment.TaxAdjustment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class PriceAdjustmentEntityMapperDirectorImpl implements PriceAdjustmentEntityMapperDirector {
    private final Map<Class<? extends AbstractPricingAdjustment<String>>, PriceAdjustmentEntityMapper> STRATEGY_MAP = new HashMap<>();

    public PriceAdjustmentEntityMapperDirectorImpl(DiscountAdjustmentEntityMapper discountAdjustmentEntityMapper,
                                                   TaxAdjustmentEntityMapper taxAdjustmentEntityMapper) {
        STRATEGY_MAP.put(DiscountAdjustment.class, discountAdjustmentEntityMapper);
        STRATEGY_MAP.put(TaxAdjustment.class, taxAdjustmentEntityMapper);
    }

    @Override
    public PriceAdjustmentEntityMapper getStrategy(AbstractPricingAdjustment<String> domain) {
        PriceAdjustmentEntityMapper strategy = STRATEGY_MAP.get(domain.getClass());
        if (Objects.isNull(strategy)) {
            throw new IllegalArgumentException("No creator found for adjustment type: " + domain.getClass().getName());
        }
        return strategy;
    }
}
