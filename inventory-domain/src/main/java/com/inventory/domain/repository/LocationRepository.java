package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {

    Optional<Location> findById(Id id);
    Optional<Location> findByCode(String code);
    Location save(Location location);
    void delete(Id id);
    boolean existsByCode(String code);
}
