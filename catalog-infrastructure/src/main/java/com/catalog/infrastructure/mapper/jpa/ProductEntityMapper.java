package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductStatus;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.catalog.infrastructure.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProductEntityMapper {
    @Mapping(ignore = true, target = ProductEntity_.ID)
    @Mapping(source = "id", target = ProductEntity_.UUID)
    @Mapping(source = "name", target = ProductEntity_.NAME)
    @Mapping(source = "categoryId", target = ProductEntity_.CATEGORY_ENTITY)
    @Mapping(source = "slug", target = ProductEntity_.SLUG)
    @Mapping(source = "featured", target = ProductEntity_.FEATURED)
    public abstract void toEntity(Product source, @MappingTarget ProductEntity destination);

    protected abstract String toProductStatus(ProductStatus status);
}
