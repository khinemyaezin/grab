package com.pricing.infrastructure.mapper.price;


import com.pricing.domain.aggregate.pricing_model.AbstractPricingModel;
import com.pricing.domain.aggregate.pricing_model.TieredPricing;
import com.pricing.infrastructure.entity.PricingStrategyEntity;
import com.pricing.infrastructure.entity.TieredPricingEntity;
import com.pricing.infrastructure.mapper.CentralMapperConfig;
import org.joda.money.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(config = CentralMapperConfig.class, uses = MoneyMapper.class)
public abstract class TieredPricingEntityMapper implements PricingStrategyEntityMapper {

    private TieredPricing getInstance(AbstractPricingModel<String> model) {
        if (!(model instanceof TieredPricing tieredPricing)) {
            throw new IllegalArgumentException("Invalid model type for TieredPricingEntityCreator");
        }
        return tieredPricing;
    }

    @Override
    public void map(AbstractPricingModel<String> model, PricingStrategyEntity entity) {
        TieredPricing tieredPricing = getInstance(model);
        TieredPricingEntity tieredPricingEntity = (TieredPricingEntity) entity;
        map(tieredPricing, tieredPricingEntity);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id")
    abstract void map(TieredPricing source, @MappingTarget TieredPricingEntity target);

    Map<Integer, BigDecimal> convertTieredPrices(Map<Integer, Money> tierPrices) {
        return tierPrices.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getAmount()
                ));
    }
}