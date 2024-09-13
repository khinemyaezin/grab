package com.coolstuff.ecommerce.grab.infrastructure.product.repository;

import java.util.Optional;

public interface EntityRepository<T> {
    Optional<T> findByUuid(String uuid);
}
