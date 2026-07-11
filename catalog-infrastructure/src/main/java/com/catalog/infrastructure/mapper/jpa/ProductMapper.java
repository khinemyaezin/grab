package com.catalog.infrastructure.mapper.jpa;

import com.catalog.infrastructure.entity.meta.ProductEntity_;
import com.grab.framework.id.IdGenerator;
import com.catalog.domain.aggregate.Product;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class ProductMapper {

    @Mapping(source = "entity." + ProductEntity_.UUID, target="id")
    @Mapping(source = "entity." + ProductEntity_.NAME , target="name")
    @Mapping(source = "entity." + ProductEntity_.MERCHANT_ID , target="merchantId")
    @Mapping(source = "entity." + ProductEntity_.CATEGORY_ENTITY , target="categoryId")
    @Mapping(source = "entity." + ProductEntity_.LISTING_CONDITION , target="listingCondition")
    @Mapping(source = "entity." + ProductEntity_.STATUS , target="status")
    @Mapping(source = "entity." + ProductEntity_.SLUG , target="slug")
    @Mapping(source = "variants" , target="variants")
    public abstract Product toDomain(ProductEntity entity, List<ProductVariant> variants);
}
