package com.inventory.adapter.persistence.mapper.jpa.impl;

import com.inventory.domain.aggregate.Zone;
import com.inventory.adapter.persistence.entity.ZoneEntity;
import com.inventory.adapter.persistence.mapper.jpa.ZoneEntityMapper;
import com.inventory.adapter.persistence.mapper.jpa.ZoneJpaAssembler;
import com.inventory.adapter.persistence.mapper.jpa.ZoneMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ZoneJpaAssemblerImpl implements ZoneJpaAssembler {
    private final ZoneEntityMapper zoneEntityMapper;
    private final ZoneMapper zoneMapper;

    @Override
    public ZoneEntity buildFullEntityGraph(Zone zone, ZoneEntity entity) {
        if (entity == null) {
            entity = new ZoneEntity();
        }
        zoneEntityMapper.toEntity(zone, entity);
        return entity;
    }

    @Override
    public Zone toFullDomainGraph(ZoneEntity entity) {
        return zoneMapper.toDomain(entity);
    }
}
