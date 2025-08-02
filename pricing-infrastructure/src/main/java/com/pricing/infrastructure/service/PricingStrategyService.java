package com.pricing.infrastructure.service;

import com.pricing.domain.aggregate.Pricing;
import com.pricing.infrastructure.entity.PricingEntity;

public interface PricingStrategyService {
    void updatePricingStrategies(PricingEntity pricingEntity, Pricing pricing);
}
