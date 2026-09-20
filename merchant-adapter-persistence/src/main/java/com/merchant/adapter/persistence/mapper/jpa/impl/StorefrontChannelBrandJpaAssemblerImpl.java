package com.merchant.adapter.persistence.mapper.jpa.impl;

import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.adapter.persistence.entity.StorefrontChannelBrandEntity;
import com.merchant.adapter.persistence.entity.StorefrontEntity;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandEntityMapper;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandJpaAssembler;
import com.merchant.adapter.persistence.mapper.jpa.StorefrontChannelBrandMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StorefrontChannelBrandJpaAssemblerImpl implements StorefrontChannelBrandJpaAssembler {
    private final StorefrontChannelBrandEntityMapper entityMapper;
    private final StorefrontChannelBrandMapper domainMapper;

    @Override
    public StorefrontChannelBrandEntity buildFullEntityGraph(
            StorefrontChannelBrand brand,
            StorefrontChannelBrandEntity entity,
            StorefrontEntity storefront
    ) {
        if (entity == null) {
            entity = new StorefrontChannelBrandEntity();
        }
        entity.setStorefrontId(storefront.getId());
        entityMapper.toEntity(brand, entity);
        return entity;
    }

    @Override
    public StorefrontChannelBrand toFullDomainGraph(
            StorefrontChannelBrandEntity entity,
            StorefrontEntity storefront
    ) {
        return domainMapper.toDomain(entity, storefront);
    }
}
