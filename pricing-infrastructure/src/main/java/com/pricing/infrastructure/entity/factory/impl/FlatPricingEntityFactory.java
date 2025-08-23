package com.pricing.infrastructure.entity.factory.impl;

import com.pricing.infrastructure.entity.FlatPricingEntity;
import com.pricing.infrastructure.entity.PricingStrategyEntity;
import com.pricing.infrastructure.entity.factory.PricingStrategyEntityFactory;
import org.springframework.stereotype.Component;

@Component
public class FlatPricingEntityFactory implements PricingStrategyEntityFactory {

    @Override
    public PricingStrategyEntity create() {
        return new FlatPricingEntity();
    }
}
