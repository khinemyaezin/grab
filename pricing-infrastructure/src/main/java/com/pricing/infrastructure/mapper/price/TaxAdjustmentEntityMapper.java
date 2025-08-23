package com.pricing.infrastructure.mapper.price;

import com.pricing.domain.aggregate.price_adjustment.AbstractPricingAdjustment;
import com.pricing.domain.aggregate.price_adjustment.TaxAdjustment;
import com.pricing.infrastructure.entity.AdjustmentType;
import com.pricing.infrastructure.entity.PriceAdjustmentEntity;
import com.pricing.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = MoneyMapper.class)
public abstract class TaxAdjustmentEntityMapper implements PriceAdjustmentEntityMapper {

    private TaxAdjustment getInstance(AbstractPricingAdjustment<String> domain) {
        if (!(domain instanceof TaxAdjustment taxAdjustment)) {
            throw new IllegalArgumentException("Invalid domain type for TaxAdjustmentEntityCreator");
        }
        return taxAdjustment;
    }

    @Override
    public PriceAdjustmentEntity map(AbstractPricingAdjustment<String> domain) {
        PriceAdjustmentEntity entity = new PriceAdjustmentEntity();
        map(domain,entity);
        return entity;
    }

    @Override
    public void map(AbstractPricingAdjustment<String> source, PriceAdjustmentEntity target) {
        TaxAdjustment taxAdjustment = getInstance(source);
        map(taxAdjustment, target);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", source = "id")
    abstract void map(TaxAdjustment source, @MappingTarget PriceAdjustmentEntity target);

    @AfterMapping
    void setAdjustmentType(@MappingTarget PriceAdjustmentEntity target){
        target.setAdjustmentType(AdjustmentType.TAX);
    }
}