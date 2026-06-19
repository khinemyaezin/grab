package com.catalog.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductVariantStatus;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class,uses = {IdGenerator.class})
public abstract class ProductVariantMapper {

    @Mapping(source = "entity.uuid." , target="id")
    @Mapping(source = "entity.sku" , target="sku")
    @Mapping(source = "variations" , target="variations")
    @Mapping(source = "entity.status" , target="status")
    public abstract ProductVariant toDomain(ProductVariantEntity entity, List<ProductVariation> variations) ;
}
