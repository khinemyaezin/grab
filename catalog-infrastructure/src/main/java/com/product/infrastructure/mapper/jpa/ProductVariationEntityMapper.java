package com.product.infrastructure.mapper.jpa;

import com.grab.framework.mapper.CommonMapper;
import com.product.domain.valueobject.ProductVariation;
import com.product.infrastructure.entity.product.entity.ProductVariationEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {CommonMapper.class})
public abstract class ProductVariationEntityMapper {
    public abstract void toEntity(ProductVariation domain, @MappingTarget ProductVariationEntity entity);
}
