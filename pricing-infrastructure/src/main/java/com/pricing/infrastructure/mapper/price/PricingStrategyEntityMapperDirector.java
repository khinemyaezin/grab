package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import com.pricing.domain.aggregate.pricing_model.CostBasedPricing;
import com.pricing.domain.aggregate.pricing_model.FlatPricing;
import com.pricing.domain.aggregate.pricing_model.TieredPricing;
import com.pricing.infrastructure.entity.PricingStrategyEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Primary
@Component
public class PricingStrategyEntityMapperDirector implements PricingStrategyEntityMapper {
    private final Map<Class<? extends AbstractPricingModel<String>>, PricingStrategyEntityMapper> STRATEGY_HASH_MAP = new HashMap<>();

    public PricingStrategyEntityMapperDirector(CostBasedPricingEntityMapper costBasedPricingEntityMapper, FlatPricingEntityCreator flatPricingEntityCreator, TieredPricingEntityMapper tieredPricingEntityMapper) {
        STRATEGY_HASH_MAP.put(CostBasedPricing.class, costBasedPricingEntityMapper);
        STRATEGY_HASH_MAP.put(FlatPricing.class, flatPricingEntityCreator);
        STRATEGY_HASH_MAP.put(TieredPricing.class, tieredPricingEntityMapper);
    }

    @Override
    public void map(AbstractPricingModel<String> model, PricingStrategyEntity entity) {
        PricingStrategyEntityMapper strategy = STRATEGY_HASH_MAP.get(model.getClass());
        if (strategy == null) {
            throw new IllegalArgumentException("No creator found for model type: " + model.getClass().getName());
        }
        strategy.map(model,entity);
    }
}
