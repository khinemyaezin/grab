package com.catalog.adapter.persistence.mapper;

import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.entity.meta.ProductEntity_;
import com.catalog.adapter.persistence.entity.meta.ProductVariantEntity_;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public interface ProductVariantEntityMapper {
    @Mapping(ignore = true, target = ProductEntity_.ID)
    @Mapping(ignore = true, target = ProductVariantEntity_.MEDIAS)
    @Mapping(ignore = true, target = ProductVariantEntity_.THUMBNAIL_MEDIA_UUID)
    @Mapping(source = "id", target = ProductVariantEntity_.UUID)
    @Mapping(source = "manageInventory", target = ProductVariantEntity_.MANAGE_INVENTORY)
    void toEntity(ProductVariant source, @MappingTarget ProductVariantEntity destination);
}
