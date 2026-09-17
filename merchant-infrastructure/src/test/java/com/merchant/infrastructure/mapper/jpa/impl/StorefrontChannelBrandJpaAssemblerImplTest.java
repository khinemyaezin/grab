package com.merchant.infrastructure.mapper.jpa.impl;

import com.grab.framework.id.impl.CommonId;
import com.merchant.domain.aggregate.StorefrontChannelBrand;
import com.merchant.infrastructure.entity.StorefrontChannelBrandEntity;
import com.merchant.infrastructure.entity.StorefrontEntity;
import com.merchant.infrastructure.mapper.jpa.StorefrontChannelBrandEntityMapper;
import com.merchant.infrastructure.mapper.jpa.StorefrontChannelBrandMapper;
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

class StorefrontChannelBrandJpaAssemblerImplTest {

    private StorefrontChannelBrandEntityMapper entityMapper;
    private StorefrontChannelBrandMapper domainMapper;
    private StorefrontChannelBrandJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        entityMapper = mock(StorefrontChannelBrandEntityMapper.class);
        domainMapper = mock(StorefrontChannelBrandMapper.class);
        assembler = new StorefrontChannelBrandJpaAssemblerImpl(entityMapper, domainMapper);
    }

    @Test
    void buildFullEntityGraph_withNoExistingEntity_createsNewEntity() {
        StorefrontChannelBrand brand = StorefrontChannelBrand.restore(
                new CommonId("sf-1"),
                new CommonId("channel-1"));
        StorefrontEntity storefront = storefrontEntity(20L, "sf-1");

        StorefrontChannelBrandEntity result = assembler.buildFullEntityGraph(brand, null, storefront);

        assertNotNull(result);
        assertEquals(20L, result.getStorefrontId());
        verify(entityMapper).toEntity(eq(brand), any(StorefrontChannelBrandEntity.class));
    }

    @Test
    void buildFullEntityGraph_withExistingEntity_mergesIntoExistingEntity() {
        StorefrontChannelBrand brand = StorefrontChannelBrand.restore(
                new CommonId("sf-1"),
                new CommonId("channel-1"));
        StorefrontEntity storefront = storefrontEntity(20L, "sf-1");
        StorefrontChannelBrandEntity existingEntity = new StorefrontChannelBrandEntity();
        existingEntity.setStorefrontId(20L);
        existingEntity.setSalesChannelId("channel-1");

        StorefrontChannelBrandEntity result = assembler.buildFullEntityGraph(brand, existingEntity, storefront);

        assertSame(existingEntity, result);
        verify(entityMapper).toEntity(same(brand), same(existingEntity));
    }

    @Test
    void toFullDomainGraph_mapsEntityToDomain() {
        StorefrontChannelBrandEntity entity = new StorefrontChannelBrandEntity();
        entity.setStorefrontId(20L);
        entity.setSalesChannelId("channel-1");
        StorefrontEntity storefront = storefrontEntity(20L, "sf-1");
        StorefrontChannelBrand expected = StorefrontChannelBrand.restore(
                new CommonId("sf-1"),
                new CommonId("channel-1"));
        when(domainMapper.toDomain(entity, storefront)).thenReturn(expected);

        StorefrontChannelBrand result = assembler.toFullDomainGraph(entity, storefront);

        assertSame(expected, result);
        verify(domainMapper).toDomain(entity, storefront);
    }

    private static StorefrontEntity storefrontEntity(Long id, String uuid) {
        StorefrontEntity storefront = new StorefrontEntity();
        storefront.setId(id);
        storefront.setUuid(uuid);
        return storefront;
    }
}
