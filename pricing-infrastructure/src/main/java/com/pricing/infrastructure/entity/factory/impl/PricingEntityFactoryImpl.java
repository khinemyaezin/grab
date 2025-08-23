package com.pricing.infrastructure.entity.factory.impl;

import com.pricing.infrastructure.entity.factory.PricingEntityFactory;
import com.pricing.infrastructure.entity.PricingEntity;
import org.springframework.stereotype.Component;

@Component
public class PricingEntityFactoryImpl implements PricingEntityFactory {
    @Override
    public PricingEntity create() {
        return new PricingEntity();
    }
}
