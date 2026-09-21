package com.inventory.adapter.persistence.mapper.jpa;

import com.inventory.domain.aggregate.Zone;
import com.inventory.adapter.persistence.entity.ZoneEntity;

public interface ZoneJpaAssembler {
    ZoneEntity buildFullEntityGraph(Zone zone, ZoneEntity entity);
    Zone toFullDomainGraph(ZoneEntity entity);
}
