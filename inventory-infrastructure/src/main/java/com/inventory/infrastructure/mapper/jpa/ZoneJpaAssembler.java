package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.aggregate.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;

public interface ZoneJpaAssembler {
    ZoneEntity toEntity(Zone zone, ZoneEntity entity);
    Zone toDomain(ZoneEntity entity);
}
