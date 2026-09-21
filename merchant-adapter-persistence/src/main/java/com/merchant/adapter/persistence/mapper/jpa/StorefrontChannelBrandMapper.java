package com.merchant.adapter.persistence.mapper.jpa;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.adapter.persistence.entity.StorefrontChannelBrandEntity;
import com.merchant.adapter.persistence.entity.StorefrontEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper(config = CentralMapperConfig.class)
public abstract class StorefrontChannelBrandMapper {

    @BeanMapping(ignoreByDefault = true)
    public abstract StorefrontChannelBrand toDomain(
            StorefrontChannelBrandEntity entity,
            StorefrontEntity storefront
    );

    @ObjectFactory
    protected StorefrontChannelBrand create(StorefrontChannelBrandEntity entity, StorefrontEntity storefront) {
        return StorefrontChannelBrand.restore(
                new CommonId(storefront.getUuid()),
                new CommonId(entity.getSalesChannelId())
        );
    }
}
