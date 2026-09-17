package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.grab.framework.id.impl.CommonId;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(config = CentralMapperConfig.class)
public abstract class ProductPublicationMapper {

    @BeanMapping(ignoreByDefault = true)
    public abstract ProductPublication toDomain(
            ProductPublicationEntity entity,
            ProductEntity product
    );

    @ObjectFactory
    protected ProductPublication create(ProductPublicationEntity entity, ProductEntity product) {
        return ProductPublication.restore(
                new CommonId(product.getUuid()),
                new CommonId(entity.getSalesChannelId()),
                null
        );
    }
}
