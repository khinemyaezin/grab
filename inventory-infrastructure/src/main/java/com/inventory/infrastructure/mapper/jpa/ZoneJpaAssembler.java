package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.aggregate.Zone;
import com.inventory.infrastructure.entity.ZoneEntity;

public interface ZoneJpaAssembler {
    ZoneEntity buildFullEntityGraph(Zone zone, ZoneEntity entity);
    Zone toFullDomainGraph(ZoneEntity entity);
}
