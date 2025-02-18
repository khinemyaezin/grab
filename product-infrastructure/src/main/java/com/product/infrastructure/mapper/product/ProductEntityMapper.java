package com.product.infrastructure.mapper.product;

import com.grab.framework.mapper.CommonMapper;
import com.product.domain.aggregate.product.Product;
import com.product.infrastructure.entity.category.entity.CategoryEntity;
import com.product.infrastructure.entity.product.entity.ProductEntity;
import com.product.infrastructure.entity.product.meta.ProductEntity_;
import com.product.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = CommonMapper.class)
public interface ProductEntityMapper {

    @Mapping(source = "source.id", target = ProductEntity_.UUID)
    @Mapping(source = "source.name", target = ProductEntity_.NAME)
    @Mapping(source = "categoryEntity", target = ProductEntity_.CATEGORY_ENTITY)
    void map(Product source, CategoryEntity categoryEntity, @MappingTarget ProductEntity destination);
}
