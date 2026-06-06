package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.aggregate.Location;
import com.inventory.infrastructure.entity.LocationEntity;

public interface LocationJpaAssembler {
    LocationEntity toEntity(Location location, LocationEntity entity);
    Location toDomain(LocationEntity entity);
}
