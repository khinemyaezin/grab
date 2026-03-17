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
    @Mapping(source = "id", target = ProductEntity_.UUID)
    @Mapping(source = "name", target = ProductEntity_.NAME)
    @Mapping(source = "categoryId", target = ProductEntity_.CATEGORY_ENTITY)
    @Mapping(source = "sellerId", target = ProductEntity_.SELLER_ID)
    @Mapping(source = "sellerType", target = ProductEntity_.SELLER_TYPE)
    @Mapping(source = "listingCondition", target = ProductEntity_.LISTING_CONDITION)
    @Mapping(source = "offerEligible", target = ProductEntity_.OFFER_ELIGIBLE)
    @Mapping(source = "moderationNote", target = ProductEntity_.MODERATION_NOTE)
    @Mapping(source = "slug", target = ProductEntity_.SLUG)
    @Mapping(source = "featured", target = ProductEntity_.FEATURED)
    public abstract void toEntity(Product source, @MappingTarget ProductEntity destination);
}
