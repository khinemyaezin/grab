package com.catalog.domain.repository;

import com.catalog.domain.aggregate.ProductPublication;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public interface ProductPublicationRepository {
    Optional<ProductPublication> find(Id productId, Id salesChannelId);

    List<ProductPublication> findByProductId(Id productId);

    boolean exists(Id productId, Id salesChannelId);

    void save(ProductPublication publication);

    void delete(ProductPublication publication);
}
