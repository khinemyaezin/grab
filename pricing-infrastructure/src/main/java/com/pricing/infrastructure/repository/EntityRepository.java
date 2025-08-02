package com.pricing.infrastructure.repository;

import java.util.Optional;

public interface EntityRepository<T,ID> {
    Optional<ID> findIdByUuid(String uuid);

}
