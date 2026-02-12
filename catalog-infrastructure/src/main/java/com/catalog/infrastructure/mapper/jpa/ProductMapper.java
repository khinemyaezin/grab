package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.ProductStatus;
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

    @Mapping(source = "entity.uuid." , target="id")
    @Mapping(source = "entity.name" , target="name")
    @Mapping(source = "entity.categoryId" , target="categoryId")
    @Mapping(source = "entity.status" , target="status")
    @Mapping(source = "entity.slug" , target="slug")
    @Mapping(source = "entity.featured" , target="featured")
    @Mapping(source = "variants" , target="variants")
    public abstract Product toDomain(ProductEntity entity, List<ProductVariant> variants);
}
