package com.pricing.infrastructure.entity.factory;

import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;

public interface PricingStrategyEntityFactoryDirector {
    PricingStrategyEntityFactory getFactory(AbstractPricingModel<String> model);
}
