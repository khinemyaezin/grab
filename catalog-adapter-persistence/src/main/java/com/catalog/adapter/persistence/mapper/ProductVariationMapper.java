package com.catalog.adapter.persistence.mapper;

import com.grab.framework.id.IdGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.adapter.persistence.entity.ProductVariationEntity;
import com.catalog.adapter.persistence.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class,uses = {IdGenerator.class})
public abstract class ProductVariationMapper {

    @Mapping(source = "id.variantOptionUuid", target = "optionId")
    @Mapping(source = "id.variantTypeUuid", target = "typeId")
    public abstract ProductVariation toDomain(ProductVariationEntity productVariationEntity);
}
