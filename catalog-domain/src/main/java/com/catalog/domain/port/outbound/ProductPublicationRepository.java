package com.catalog.domain.port.outbound;

import com.catalog.domain.aggregate.ProductPublication;
import com.grab.framework.id.Id;

import java.util.List;
import java.util.Optional;

public interface ProductPublicationRepository {
    Optional<ProductPublication> find(Id variantId, Id salesChannelId);

    List<ProductPublication> findByVariantId(Id variantId);

    List<ProductPublication> findByProductId(Id productId);

    boolean exists(Id variantId, Id salesChannelId);

    void save(ProductPublication publication);

    void delete(ProductPublication publication);
}
