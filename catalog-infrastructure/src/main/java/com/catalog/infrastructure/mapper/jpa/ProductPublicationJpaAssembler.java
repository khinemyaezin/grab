package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;

public interface ProductPublicationJpaAssembler {

    ProductPublicationEntity buildFullEntityGraph(
            ProductPublication publication,
            ProductPublicationEntity entity,
            ProductVariantEntity variant
    );

    ProductPublication toFullDomainGraph(
            ProductPublicationEntity entity,
            ProductVariantEntity variant
    );
}
