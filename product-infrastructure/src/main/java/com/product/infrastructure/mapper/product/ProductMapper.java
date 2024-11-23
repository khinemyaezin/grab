package com.product.infrastructure.mapper.product;

import com.product.domain.entity.product.Product;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface ProductMapper {

    void map(Product source, @MappingTarget ProductEntity product);
}
