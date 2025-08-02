package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.Pricing;
import com.pricing.infrastructure.entity.PricingEntity;
import com.pricing.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {MoneyMapper.class, CurrencyMapper.class})
public interface PricingEntityMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "uuid")
    @Mapping(source = "productId", target = "product")
    @Mapping(ignore = true, target = "pricingStrategies")
    @Mapping(ignore = true, target = "priceAdjustments")
    void map(Pricing source, @MappingTarget PricingEntity target);
}
