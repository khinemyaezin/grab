package com.inventory.adapter.persistence.mapper;

import com.grab.framework.mapper.IdMapper;
import com.inventory.adapter.persistence.entity.ProductVariantViewEntity;
import com.inventory.application.model.write.ProductVariantProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class ProductVariantProjectionMapper {
    @Mapping(target = "productId", source = "productUuid")
    @Mapping(target = "variantId", source = "variantUuid")
    public abstract ProductVariantProjection toProjection(ProductVariantViewEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "productUuid", source = "productId")
    @Mapping(target = "variantUuid", source = "variantId")
    public abstract void updateEntity(ProductVariantProjection projection, @MappingTarget ProductVariantViewEntity entity);
}
