package com.inventory.domain.repository;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.Bin;

import java.util.List;
import java.util.Optional;

public interface BinRepository {

    Optional<Bin> findById(Id id);

    Optional<Bin> findByCodeAndZoneId(String code, Id zoneId);

    Bin save(Bin bin);

    void delete(Id id);

    boolean existsByCodeAndZoneId(String code, Id zoneId);
}
