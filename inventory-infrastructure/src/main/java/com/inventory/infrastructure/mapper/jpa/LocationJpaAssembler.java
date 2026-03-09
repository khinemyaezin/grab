package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.aggregate.Location;
import com.inventory.infrastructure.entity.LocationEntity;

public interface LocationJpaAssembler {
    /**
     * If `entity` is null a new LocationEntity instance will be created; otherwise the provided `entity` is updated.
     *
     * @param location the full Location aggregate to persist
     * @param entity the existing LocationEntity to update, or null to create a new instance
     * @return the resulting LocationEntity populated from the aggregate
     */
    LocationEntity buildFullEntityGraph(Location location, LocationEntity entity);

    /**
     * Reconstruct the full Location domain aggregate from the provided JPA entity.
     *
     * @param locationEntity the persisted LocationEntity to convert (may not be null)
     * @return a fully populated Location domain aggregate
     */
    Location toFullDomainGraph(LocationEntity locationEntity);
}
