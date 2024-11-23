package com.product.infrastructure.mapper.product;

import com.product.domain.entity.product.Product;
import com.product.infrastructure.entity.category.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class)
public interface ProductEntityMapper {

    @Mapping(source = "source.id", target = "uuid")
    @Mapping(source = "source.name", target = "name")
    @Mapping(source = "categoryEntity", target = "category")
    void map(Product source, CategoryEntity categoryEntity, @MappingTarget ProductEntity destination);
}
