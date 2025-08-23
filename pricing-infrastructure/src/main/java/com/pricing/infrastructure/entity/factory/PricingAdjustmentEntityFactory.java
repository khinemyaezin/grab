package com.pricing.infrastructure.entity.factory;

import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;
import com.pricing.infrastructure.entity.PriceAdjustmentEntity;

public interface PricingAdjustmentEntityFactory {
    PriceAdjustmentEntity create(AbstractPricingAdjustment<String> domain);
}
