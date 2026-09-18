package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductEntity;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.mapper.jpa.ProductPublicationEntityMapper;
import com.catalog.infrastructure.mapper.jpa.ProductPublicationMapper;
import com.grab.framework.id.impl.CommonId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductPublicationJpaAssemblerImplTest {

    private ProductPublicationEntityMapper entityMapper;
    private ProductPublicationMapper domainMapper;
    private ProductPublicationJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        entityMapper = mock(ProductPublicationEntityMapper.class);
        domainMapper = mock(ProductPublicationMapper.class);
        assembler = new ProductPublicationJpaAssemblerImpl(entityMapper, domainMapper);
    }

    @Test
    void buildFullEntityGraph_withNoExistingEntity_createsNewEntity() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("p1"),
                new CommonId("channel-1"),
                null);
        ProductEntity product = productEntity(20L, "p1");

        ProductPublicationEntity result = assembler.buildFullEntityGraph(publication, null, product);

        assertNotNull(result);
        assertEquals(20L, result.getProductId());
        verify(entityMapper).toEntity(eq(publication), any(ProductPublicationEntity.class));
    }

    @Test
    void buildFullEntityGraph_withExistingEntity_mergesIntoExistingEntity() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("p1"),
                new CommonId("channel-1"),
                null);
        ProductEntity product = productEntity(20L, "p1");
        ProductPublicationEntity existingEntity = new ProductPublicationEntity();
        existingEntity.setProductId(20L);
        existingEntity.setSalesChannelId("channel-1");

        ProductPublicationEntity result = assembler.buildFullEntityGraph(publication, existingEntity, product);

        assertSame(existingEntity, result);
        verify(entityMapper).toEntity(same(publication), same(existingEntity));
    }

    @Test
    void toFullDomainGraph_mapsEntityToDomain() {
        ProductPublicationEntity entity = new ProductPublicationEntity();
        entity.setProductId(20L);
        entity.setSalesChannelId("channel-1");
        ProductEntity product = productEntity(20L, "p1");
        ProductPublication expected = ProductPublication.restore(
                new CommonId("p1"),
                new CommonId("channel-1"),
                null);
        when(domainMapper.toDomain(entity, product)).thenReturn(expected);

        ProductPublication result = assembler.toFullDomainGraph(entity, product);

        assertSame(expected, result);
        verify(domainMapper).toDomain(entity, product);
    }

    private static ProductEntity productEntity(Long id, String uuid) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setUuid(uuid);
        return product;
    }
}
