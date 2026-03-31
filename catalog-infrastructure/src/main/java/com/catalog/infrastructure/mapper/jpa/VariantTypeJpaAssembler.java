package com.catalog.infrastructure.mapper.jpa;

import com.catalog.domain.aggregate.VariantType;
import com.catalog.infrastructure.entity.entity.VariantTypeEntity;

public interface VariantTypeJpaAssembler {
    VariantTypeEntity buildFullEntityGraph(VariantType variantType, VariantTypeEntity entity);

    VariantType toFullDomainGraph(VariantTypeEntity entity);
}
