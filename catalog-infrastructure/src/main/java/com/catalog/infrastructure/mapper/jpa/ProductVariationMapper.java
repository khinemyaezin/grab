package com.catalog.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.infrastructure.entity.entity.ProductVariationEntity;
import com.catalog.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class,uses = {IdGenerator.class})
public abstract class ProductVariationMapper {

    @Mapping(source = "variantOptionValue", target = "optionName")
    @Mapping(source = "id.variantOptionUuid", target = "optionId")
    @Mapping(source = "id.variantTypeUuid", target = "typeId")
    @Mapping(source = "variantTypeValue", target = "typeName")
    public abstract ProductVariation toDomain(ProductVariationEntity productVariationEntity);
}
