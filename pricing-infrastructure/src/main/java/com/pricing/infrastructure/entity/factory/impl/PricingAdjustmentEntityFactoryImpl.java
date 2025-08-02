package com.pricing.infrastructure.entity.factory.impl;

import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;
import com.pricing.infrastructure.entity.PriceAdjustmentEntity;
import com.pricing.infrastructure.entity.factory.PricingAdjustmentEntityFactory;
import com.pricing.infrastructure.mapper.price.PriceAdjustmentEntityMapper;
import com.pricing.infrastructure.mapper.price.PriceAdjustmentEntityMapperDirector;
import org.springframework.stereotype.Component;

@Component
public class PricingAdjustmentEntityFactoryImpl implements PricingAdjustmentEntityFactory {
    private final PriceAdjustmentEntityMapperDirector factory;

    public PricingAdjustmentEntityFactoryImpl(PriceAdjustmentEntityMapperDirector factory) {
        this.factory = factory;
    }

    public PriceAdjustmentEntity create(AbstractPricingAdjustment<String> domain){
        PriceAdjustmentEntityMapper strategy = factory.getStrategy(domain);
        return strategy.map(domain);
    }
}
