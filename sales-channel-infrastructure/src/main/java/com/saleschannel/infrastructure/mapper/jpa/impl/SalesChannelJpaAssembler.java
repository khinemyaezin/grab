package com.saleschannel.infrastructure.mapper.jpa.impl;

import com.grab.framework.mapper.IdMapper;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.infrastructure.entity.SalesChannelEntity;
import com.saleschannel.infrastructure.mapper.jpa.SalesChannelEntityMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SalesChannelJpaAssembler {
    private final SalesChannelEntityMapper entityMapper;
    private final IdMapper ids;

    public SalesChannelEntity toEntity(SalesChannel source, SalesChannelEntity destination) {
        SalesChannelEntity entity = destination == null ? new SalesChannelEntity() : destination;
        entityMapper.toEntity(source, entity);
        return entity;
    }

    public SalesChannel toDomain(SalesChannelEntity source) {
        return new SalesChannel(
                ids.map(source.getUuid()),
                source.getName(),
                source.getType(),
                source.getOwner(),
                ids.map(source.getMerchantId()),
                source.getStatus(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getVersion()
        );
    }
}
