package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.Product;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ProductEntityMapper {
    @Mapping(ignore = true, target = ProductEntity_.ID)
    @Mapping(ignore = true, target = ProductEntity_.MEDIA_ENTITIES)
    @Mapping(ignore = true, target = ProductEntity_.DESCRIPTION_ENTITIES)
    @Mapping(ignore = true, target = ProductEntity_.PRODUCT_VARIANT_ENTITIES)
    @Mapping(source = "id", target = ProductEntity_.UUID)
    @Mapping(source = "name", target = ProductEntity_.NAME)
    @Mapping(source = "merchantId", target = ProductEntity_.MERCHANT_ID)
    @Mapping(source = "categoryId", target = ProductEntity_.CATEGORY_ENTITY)
    @Mapping(source = "listingCondition", target = ProductEntity_.LISTING_CONDITION)
    @Mapping(source = "slug", target = ProductEntity_.SLUG)
    @Mapping(source = "featured", target = ProductEntity_.FEATURED)
    public abstract void toEntity(Product source, @MappingTarget ProductEntity destination);
}
