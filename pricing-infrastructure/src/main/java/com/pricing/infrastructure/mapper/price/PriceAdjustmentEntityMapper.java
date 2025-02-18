package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;
import com.pricing.infrastructure.entity.PriceAdjustmentEntity;

public interface PriceAdjustmentEntityMapper {
    PriceAdjustmentEntity map(AbstractPricingAdjustment<String> domain);
    void map(AbstractPricingAdjustment<String> source,PriceAdjustmentEntity target);
}
