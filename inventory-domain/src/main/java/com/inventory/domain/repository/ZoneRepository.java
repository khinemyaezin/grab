package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.Zone;

import java.util.List;
import java.util.Optional;

public interface ZoneRepository {

    Optional<Zone> findById(Id id);

    List<Zone> findAllActiveByLocationId(Id locationId);

    Zone save(Zone zone);

    void delete(Id id);

    boolean existsByCodeAndLocationId(String code, Id locationId);

    boolean existsByLocationId(Id locationId);
}
