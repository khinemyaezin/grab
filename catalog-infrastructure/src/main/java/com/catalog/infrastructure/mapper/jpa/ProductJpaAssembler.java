package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.Product;
import com.catalog.infrastructure.entity.entity.ProductEntity;

public interface ProductJpaAssembler {
    /**
      * Convert a Product domain aggregate into a persistable ProductEntity, including all related entities.
      * If `entity` is null a new ProductEntity instance will be created; otherwise the provided `entity` is updated.
      *
      * @param product the full Product aggregate to persist
      * @param entity the existing ProductEntity to update, or null to create a new instance
      * @return the resulting ProductEntity populated from the aggregate
      */
    ProductEntity buildFullEntityGraph(Product product, ProductEntity entity);

    /**
     * Reconstruct the full Product domain aggregate from the provided JPA entity,
     *
     * @param productJpaEntity the persisted ProductEntity to convert (may not be null)
     * @return a fully populated Product domain aggregate
     */
    Product toFullDomainGraph(ProductEntity productJpaEntity);
}
