package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import com.pricing.infrastructure.entity.PricingStrategyEntity;

public interface PricingStrategyEntityMapper {
    void map(AbstractPricingModel<String> model, PricingStrategyEntity entity);
}
