package com.catalog.adapter.persistence.mapper;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;

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
