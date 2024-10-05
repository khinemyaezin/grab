package com.coolstuff.ecommerce.grab.infrastructure.product.repository;

import java.util.Optional;

public interface EntityRepository<T,ID> {
    Optional<T> findByUuid(String uuid);
    Optional<ID> findIdByUuid(String id);
}
