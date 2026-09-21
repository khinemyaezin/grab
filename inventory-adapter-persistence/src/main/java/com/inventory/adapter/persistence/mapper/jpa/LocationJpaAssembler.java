package com.inventory.adapter.persistence.mapper.jpa;

import com.inventory.domain.aggregate.Location;
import com.inventory.adapter.persistence.entity.LocationEntity;

public interface LocationJpaAssembler {
    LocationEntity buildFullEntityGraph(Location location, LocationEntity entity);
    Location toFullDomainGraph(LocationEntity entity);
}
