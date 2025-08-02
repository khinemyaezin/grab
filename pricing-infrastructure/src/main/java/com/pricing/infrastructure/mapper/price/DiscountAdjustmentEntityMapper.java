package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;
import com.pricing.domain.aggregate.price_adjustment.DiscountAdjustment;
import com.pricing.infrastructure.entity.AdjustmentType;
import com.pricing.infrastructure.entity.PriceAdjustmentEntity;
import com.pricing.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = MoneyMapper.class)
public abstract class DiscountAdjustmentEntityMapper implements PriceAdjustmentEntityMapper {

    private DiscountAdjustment getInstance(AbstractPricingAdjustment<String> domain){
        if (!(domain instanceof DiscountAdjustment discountAdjustment)) {
            throw new IllegalArgumentException("Invalid domain type for DiscountAdjustmentEntityCreator");
        }
        return discountAdjustment;
    }

    @Override
    public PriceAdjustmentEntity map(AbstractPricingAdjustment<String> domain) {
        DiscountAdjustment discountAdjustment = getInstance(domain);
        PriceAdjustmentEntity entity = new PriceAdjustmentEntity();
        map(discountAdjustment, entity);
        return entity;
    }

    @Override
    public void map(AbstractPricingAdjustment<String> source, PriceAdjustmentEntity target) {
        DiscountAdjustment discountAdjustment = getInstance(source);
        map(discountAdjustment, target);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id")
    abstract void map(DiscountAdjustment source, @MappingTarget PriceAdjustmentEntity target);

    @AfterMapping
    void setAdjustmentType(@MappingTarget PriceAdjustmentEntity target){
        target.setAdjustmentType(AdjustmentType.DISCOUNT);
    }
}
