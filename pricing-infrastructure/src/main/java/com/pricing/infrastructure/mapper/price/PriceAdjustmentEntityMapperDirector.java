package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;

public interface PriceAdjustmentEntityMapperDirector {
    PriceAdjustmentEntityMapper getStrategy(AbstractPricingAdjustment<String> domain);
}
