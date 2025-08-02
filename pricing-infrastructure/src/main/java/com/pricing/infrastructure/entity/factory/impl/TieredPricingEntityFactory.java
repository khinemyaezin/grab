package com.pricing.infrastructure.entity.factory.impl;

import com.pricing.infrastructure.entity.PricingStrategyEntity;
import com.pricing.infrastructure.entity.TieredPricingEntity;
import com.pricing.infrastructure.entity.factory.PricingStrategyEntityFactory;
import org.springframework.stereotype.Component;

@Component
public class TieredPricingEntityFactory implements PricingStrategyEntityFactory {

    @Override
    public PricingStrategyEntity create() {
        return new TieredPricingEntity();
    }
}
