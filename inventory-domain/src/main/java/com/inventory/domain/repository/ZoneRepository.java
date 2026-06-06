package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.Zone;

import java.util.Optional;

public interface ZoneRepository {

    Optional<Zone> findById(Id id);

    Zone save(Zone zone);

    void delete(Id id);

    boolean existsByCodeAndLocationId(String code, Id locationId);
}
