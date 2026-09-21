package com.catalog.adapter.persistence.mapper;

import com.grab.framework.id.IdGenerator;
import com.catalog.domain.aggregate.ProductVariant;
import com.catalog.domain.valueobject.ProductVariation;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.mapper.CentralMapperConfig;
import com.grab.framework.id.Id;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;

@Mapper(config = CentralMapperConfig.class,uses = {IdGenerator.class})
public abstract class ProductVariantMapper {

    @Mapping(source = "entity.uuid." , target="id")
    @Mapping(source = "entity.sku" , target="sku")
    @Mapping(source = "variations" , target="variations")
    @Mapping(source = "entity.status" , target="status")
    @Mapping(source = "entity.manageInventory", target = "manageInventory")
    @Mapping(source = "mediaIds", target = "mediaIds")
    @Mapping(source = "thumbnailMediaId", target = "thumbnailMediaId")
    public abstract ProductVariant toDomain(
            ProductVariantEntity entity,
            List<ProductVariation> variations,
            Collection<Id> mediaIds,
            Id thumbnailMediaId
    );
}
