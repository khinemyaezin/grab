package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
import com.grab.framework.id.impl.CommonId;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(config = CentralMapperConfig.class)
public abstract class ProductPublicationMapper {

    @BeanMapping(ignoreByDefault = true)
    public abstract ProductPublication toDomain(
            ProductPublicationEntity entity,
            ProductVariantEntity variant
    );

    @ObjectFactory
    protected ProductPublication create(ProductPublicationEntity entity, ProductVariantEntity variant) {
        return ProductPublication.restore(
                new CommonId(variant.getUuid()),
                new CommonId(entity.getSalesChannelId()),
                null
        );
    }
}
