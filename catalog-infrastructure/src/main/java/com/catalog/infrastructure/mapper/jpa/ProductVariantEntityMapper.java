package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.catalog.infrastructure.entity.meta.ProductVariantEntity_;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public interface ProductVariantEntityMapper {
    @Mapping(ignore = true, target = ProductEntity_.ID)
    @Mapping(source = "id", target = ProductVariantEntity_.UUID)
    void toEntity(ProductVariant source, @MappingTarget ProductVariantEntity destination);
}
