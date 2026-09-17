package com.merchant.infrastructure.mapper.jpa.impl;

import com.grab.framework.mapper.IdMapper;
import com.merchant.domain.aggregate.Storefront;
import com.merchant.domain.valueobject.LifecycleReason;
import com.merchant.domain.valueobject.StorefrontName;
import com.merchant.domain.valueobject.StorefrontSlug;
import com.merchant.infrastructure.entity.StorefrontEntity;
import com.merchant.infrastructure.mapper.jpa.StorefrontEntityMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StorefrontJpaAssembler {
    private final StorefrontEntityMapper entityMapper;
    private final IdMapper ids;

    public StorefrontEntity toEntity(Storefront source, StorefrontEntity destination) {
        StorefrontEntity entity = destination == null ? new StorefrontEntity() : destination;
        entityMapper.toEntity(source, entity);
        return entity;
    }

    public Storefront toDomain(StorefrontEntity source) {
        return new Storefront(
                ids.map(source.getUuid()),
                ids.map(source.getMerchantId()),
                new StorefrontName(source.getName()),
                new StorefrontSlug(source.getSlug()),
                source.getStatus(),
                source.getLifecycleReason() == null ? null : new LifecycleReason(source.getLifecycleReason()),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getVersion()
        );
    }
}
