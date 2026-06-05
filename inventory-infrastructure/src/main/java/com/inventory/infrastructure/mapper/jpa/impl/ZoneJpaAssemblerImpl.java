package com.inventory.infrastructure.mapper.jpa.impl;

import com.inventory.domain.aggregate.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.mapper.jpa.ZoneEntityMapper;
import com.inventory.infrastructure.mapper.jpa.ZoneJpaAssembler;
import com.inventory.infrastructure.mapper.jpa.ZoneMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ZoneJpaAssemblerImpl implements ZoneJpaAssembler {
    private final ZoneEntityMapper zoneEntityMapper;
    private final ZoneMapper zoneMapper;

    @Override
    public ZoneEntity toEntity(Zone zone, ZoneEntity entity) {
        if (entity == null) {
            entity = new ZoneEntity();
        }
        zoneEntityMapper.toEntity(zone, entity);
        return entity;
    }

    @Override
    public Zone toDomain(ZoneEntity entity) {
        return zoneMapper.toDomain(entity);
    }
}
