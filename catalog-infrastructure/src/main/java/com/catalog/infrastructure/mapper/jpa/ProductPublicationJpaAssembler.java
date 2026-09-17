package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;

public interface ProductPublicationJpaAssembler {

    ProductPublicationEntity buildFullEntityGraph(
            ProductPublication publication,
            ProductPublicationEntity entity,
            ProductEntity product
    );

    ProductPublication toFullDomainGraph(
            ProductPublicationEntity entity,
            ProductEntity product
    );
}
