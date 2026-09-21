package com.catalog.adapter.persistence.mapper;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
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
