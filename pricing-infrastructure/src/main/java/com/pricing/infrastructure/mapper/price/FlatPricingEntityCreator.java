package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import com.pricing.domain.aggregate.pricing_model.FlatPricing;
import com.pricing.infrastructure.entity.PricingStrategyEntity;
import com.pricing.infrastructure.entity.FlatPricingEntity;
import com.pricing.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = MoneyMapper.class)
public abstract class FlatPricingEntityCreator implements PricingStrategyEntityMapper {

    private FlatPricing getInstance(AbstractPricingModel<String> model) {
        if (!(model instanceof FlatPricing flatPricing)) {
            throw new IllegalArgumentException("Invalid model type for FlatPricingEntityCreator");
        }
        return flatPricing;
    }

    @Override
    public void map(AbstractPricingModel<String> model, PricingStrategyEntity entity) {
        FlatPricing flatPricing = getInstance(model);
        FlatPricingEntity flatPricingEntity = (FlatPricingEntity) entity;
        flatPricingEntity.setUuid(flatPricing.getId());
        flatPricingEntity.setFlatPrice(flatPricing.getPrice().toBigMoney().getAmount());
    }
}