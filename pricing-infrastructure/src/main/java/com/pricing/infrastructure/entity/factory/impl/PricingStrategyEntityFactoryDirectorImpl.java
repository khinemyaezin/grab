package com.pricing.infrastructure.entity.factory.impl;

import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import com.pricing.domain.aggregate.pricing_model.CostBasedPricing;
import com.pricing.domain.aggregate.pricing_model.FlatPricing;
import com.pricing.domain.aggregate.pricing_model.TieredPricing;
import com.pricing.infrastructure.entity.factory.PricingStrategyEntityFactory;
import com.pricing.infrastructure.entity.factory.PricingStrategyEntityFactoryDirector;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class PricingStrategyEntityFactoryDirectorImpl implements PricingStrategyEntityFactoryDirector {
    private final Map<Class<? extends AbstractPricingModel<String>>, PricingStrategyEntityFactory> FACTORY_MAP = new HashMap<>();

    public PricingStrategyEntityFactoryDirectorImpl(
            CostBasedPricingEntityFactory costBasedPricingEntityFactory,
            FlatPricingEntityFactory flatPricingEntityFactory,
            TieredPricingEntityFactory tieredPricingEntityFactory
    ) {
        FACTORY_MAP.put(CostBasedPricing.class, costBasedPricingEntityFactory);
        FACTORY_MAP.put(FlatPricing.class, flatPricingEntityFactory);
        FACTORY_MAP.put(TieredPricing.class, tieredPricingEntityFactory);
    }

    @Override
    public PricingStrategyEntityFactory getFactory(AbstractPricingModel<String> model) {
        PricingStrategyEntityFactory factory = FACTORY_MAP.get(model.getClass());
        if (Objects.isNull(factory)) {
            throw new IllegalArgumentException("No creator found for adjustment type: " + model.getClass().getName());
        }
        return factory;
    }
}
