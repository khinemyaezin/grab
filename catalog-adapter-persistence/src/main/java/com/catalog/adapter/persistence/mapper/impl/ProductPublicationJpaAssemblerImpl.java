package com.catalog.adapter.persistence.mapper.impl;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.adapter.persistence.entity.ProductPublicationEntity;
import com.catalog.adapter.persistence.entity.ProductVariantEntity;
import com.catalog.adapter.persistence.mapper.ProductPublicationEntityMapper;
import com.catalog.adapter.persistence.mapper.ProductPublicationJpaAssembler;
import com.catalog.adapter.persistence.mapper.ProductPublicationMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductPublicationJpaAssemblerImpl implements ProductPublicationJpaAssembler {
    private final ProductPublicationEntityMapper entityMapper;
    private final ProductPublicationMapper domainMapper;

    @Override
    public ProductPublicationEntity buildFullEntityGraph(
            ProductPublication publication,
            ProductPublicationEntity entity,
            ProductVariantEntity variant
    ) {
        if (entity == null) {
            entity = new ProductPublicationEntity();
        }
        entity.setVariantId(variant.getId());
        entityMapper.toEntity(publication, entity);
        return entity;
    }

    @Override
    public ProductPublication toFullDomainGraph(
            ProductPublicationEntity entity,
            ProductVariantEntity variant
    ) {
        return domainMapper.toDomain(entity, variant);
    }
}
