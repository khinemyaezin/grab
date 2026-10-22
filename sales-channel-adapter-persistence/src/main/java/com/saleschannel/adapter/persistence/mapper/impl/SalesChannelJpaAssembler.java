package com.saleschannel.adapter.persistence.mapper.impl;

import com.grab.framework.mapper.IdMapper;
import com.saleschannel.domain.aggregate.SalesChannel;
import com.saleschannel.adapter.persistence.entity.SalesChannelEntity;
import com.saleschannel.adapter.persistence.mapper.SalesChannelEntityMapper;
import com.saleschannel.adapter.persistence.entity.ChannelRegionEntity;
import com.saleschannel.adapter.persistence.entity.ChannelRegionId;
import com.saleschannel.domain.valueobject.ChannelRegion;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class SalesChannelJpaAssembler {
    private final SalesChannelEntityMapper entityMapper;
    private final IdMapper ids;

    public SalesChannelEntity toEntity(SalesChannel source, SalesChannelEntity destination) {
        SalesChannelEntity entity = destination == null ? new SalesChannelEntity() : destination;
        entityMapper.toEntity(source, entity);
        mapSupportedRegions(source, entity);
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
                source.getVersion(),
                Set.copyOf(mapSupportedRegions(source))
        );
    }

    public void clearDefaultRegions(SalesChannelEntity entity) {
        for (ChannelRegionEntity binding : entity.getSupportedRegions()) {
            binding.setDefaultRegion(false);
            binding.setUpdatedAt(Instant.now());
        }
    }

    private void mapSupportedRegions(SalesChannel source, SalesChannelEntity destination) {
        List<ChannelRegionEntity> bindings = destination.getSupportedRegions();
        Map<String, ChannelRegionEntity> existing = new HashMap<>();
        for (ChannelRegionEntity binding : bindings) {
            existing.put(binding.getId().getRegionId(), binding);
        }
        Instant now = Instant.now();
        for (ChannelRegion region : source.getSupportedRegions()) {
            String regionId = region.regionId().getValue();
            ChannelRegionEntity entity = existing.remove(regionId);
            if (entity == null) {
                entity = new ChannelRegionEntity();
                entity.setId(new ChannelRegionId(source.getId().getValue(), regionId));
                entity.setSalesChannel(destination);
                entity.setCreatedAt(now);
                bindings.add(entity);
            }
            entity.setSalesChannel(destination);
            entity.setEnabled(region.enabled());
            entity.setDefaultRegion(region.isDefault());
            entity.setUpdatedAt(now);
        }
        bindings.removeAll(existing.values());
    }

    private List<ChannelRegion> mapSupportedRegions(SalesChannelEntity source) {
        List<ChannelRegion> bindings = new ArrayList<>();
        for (ChannelRegionEntity binding : source.getSupportedRegions()) {
            bindings.add(new ChannelRegion(
                    ids.map(binding.getId().getSalesChannelId()),
                    ids.map(binding.getId().getRegionId()),
                    binding.isEnabled(),
                    binding.isDefaultRegion()
            ));
        }
        return List.copyOf(bindings);
    }
}
