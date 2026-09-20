package com.catalog.adapter.persistence.repository;

import java.util.Optional;

public interface EntityRepository<T,ID> {
    Optional<ID> findIdByUuid(String uuid);
}
