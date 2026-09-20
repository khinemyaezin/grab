package com.catalog.adapter.persistence.mapper;

import com.catalog.domain.aggregate.VariantType;
import com.catalog.adapter.persistence.entity.VariantTypeEntity;

public interface VariantTypeJpaAssembler {
    VariantTypeEntity buildFullEntityGraph(VariantType variantType, VariantTypeEntity entity);

    VariantType toFullDomainGraph(VariantTypeEntity entity);
}
