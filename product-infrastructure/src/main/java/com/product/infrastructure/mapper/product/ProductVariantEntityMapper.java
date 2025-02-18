package com.product.infrastructure.mapper.product;

import com.grab.framework.mapper.CommonMapper;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.infrastructure.entity.product.entity.ProductVariantEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class,uses = CommonMapper.class)
public interface ProductVariantEntityMapper {
    @Mapping(source = "id", target = "uuid")
    void map(ProductVariant source, @MappingTarget ProductVariantEntity destination);
}
