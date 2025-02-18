package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import com.pricing.domain.aggregate.pricing_model.CostBasedPricing;
import com.pricing.infrastructure.entity.PricingStrategyEntity;
import com.pricing.infrastructure.entity.CostBasedPricingEntity;
import com.pricing.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = MoneyMapper.class)
public abstract class CostBasedPricingEntityMapper implements PricingStrategyEntityMapper {

    private CostBasedPricing getInstance(AbstractPricingModel<String> model) {
        if (!(model instanceof CostBasedPricing costBasedPricing)) {
            throw new IllegalArgumentException("Invalid model type for CostBasedPricingEntityCreator");
        }
        return costBasedPricing;
    }

    @Override
    public void map(AbstractPricingModel<String> model, PricingStrategyEntity entity) {
        CostBasedPricing costBasedPricing = getInstance(model);
        map(costBasedPricing, (CostBasedPricingEntity) entity);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id",target = "uuid")
    @Mapping(source = "domain.markupPercentage", target = "markupPercentage")
    @Mapping(source = "domain.basePrice", target = "basePrice")
    public abstract void map(CostBasedPricing domain, @MappingTarget CostBasedPricingEntity entity);
}

