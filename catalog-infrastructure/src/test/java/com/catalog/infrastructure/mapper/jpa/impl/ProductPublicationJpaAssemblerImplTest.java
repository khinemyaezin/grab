package com.catalog.infrastructure.mapper.jpa.impl;

import com.catalog.domain.aggregate.ProductPublication;
import com.catalog.infrastructure.entity.entity.ProductPublicationEntity;
import com.catalog.infrastructure.entity.entity.ProductVariantEntity;
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
                new CommonId("var-1"),
                new CommonId("channel-1"),
                null);
        ProductVariantEntity variant = variantEntity(30L, "var-1");

        ProductPublicationEntity result = assembler.buildFullEntityGraph(publication, null, variant);

        assertNotNull(result);
        assertEquals(30L, result.getVariantId());
        verify(entityMapper).toEntity(eq(publication), any(ProductPublicationEntity.class));
    }

    @Test
    void buildFullEntityGraph_withExistingEntity_mergesIntoExistingEntity() {
        ProductPublication publication = ProductPublication.restore(
                new CommonId("var-1"),
                new CommonId("channel-1"),
                null);
        ProductVariantEntity variant = variantEntity(30L, "var-1");
        ProductPublicationEntity existingEntity = new ProductPublicationEntity();
        existingEntity.setVariantId(30L);
        existingEntity.setSalesChannelId("channel-1");

        ProductPublicationEntity result = assembler.buildFullEntityGraph(publication, existingEntity, variant);

        assertSame(existingEntity, result);
        verify(entityMapper).toEntity(same(publication), same(existingEntity));
    }

    @Test
    void toFullDomainGraph_mapsEntityToDomain() {
        ProductPublicationEntity entity = new ProductPublicationEntity();
        entity.setVariantId(30L);
        entity.setSalesChannelId("channel-1");
        ProductVariantEntity variant = variantEntity(30L, "var-1");
        ProductPublication expected = ProductPublication.restore(
                new CommonId("var-1"),
                new CommonId("channel-1"),
                null);
        when(domainMapper.toDomain(entity, variant)).thenReturn(expected);

        ProductPublication result = assembler.toFullDomainGraph(entity, variant);

        assertSame(expected, result);
        verify(domainMapper).toDomain(entity, variant);
    }

    private static ProductVariantEntity variantEntity(Long id, String uuid) {
        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setId(id);
        variant.setUuid(uuid);
        return variant;
    }
}
