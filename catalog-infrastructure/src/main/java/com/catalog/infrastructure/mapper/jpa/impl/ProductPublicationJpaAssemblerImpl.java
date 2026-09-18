package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.mapper.jpa.ProductPublicationEntityMapper;
import com.catalog.infrastructure.mapper.jpa.ProductPublicationJpaAssembler;
import com.catalog.infrastructure.mapper.jpa.ProductPublicationMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductPublicationJpaAssemblerImpl implements ProductPublicationJpaAssembler {
    private final ProductPublicationEntityMapper entityMapper;
    private final ProductPublicationMapper domainMapper;

    @Override
    public ProductPublicationEntity buildFullEntityGraph(
            ProductPublication publication,
            ProductPublicationEntity entity,
            ProductEntity product
    ) {
        if (entity == null) {
            entity = new ProductPublicationEntity();
        }
        entity.setProductId(product.getId());
        entityMapper.toEntity(publication, entity);
        return entity;
    }

    @Override
    public ProductPublication toFullDomainGraph(
            ProductPublicationEntity entity,
            ProductEntity product
    ) {
        return domainMapper.toDomain(entity, product);
    }
}
