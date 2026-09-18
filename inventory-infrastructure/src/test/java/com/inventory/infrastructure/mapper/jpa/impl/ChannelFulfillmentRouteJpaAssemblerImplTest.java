package com.inventory.infrastructure.mapper.jpa.impl;

import com.grab.framework.id.impl.CommonId;
import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.infrastructure.entity.ChannelFulfillmentRouteEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.ChannelFulfillmentRouteEntityMapper;
import com.inventory.infrastructure.mapper.jpa.ChannelFulfillmentRouteMapper;
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

class ChannelFulfillmentRouteJpaAssemblerImplTest {

    private ChannelFulfillmentRouteEntityMapper entityMapper;
    private ChannelFulfillmentRouteMapper domainMapper;
    private ChannelFulfillmentRouteJpaAssemblerImpl assembler;

    @BeforeEach
    void setUp() {
        entityMapper = mock(ChannelFulfillmentRouteEntityMapper.class);
        domainMapper = mock(ChannelFulfillmentRouteMapper.class);
        assembler = new ChannelFulfillmentRouteJpaAssemblerImpl(entityMapper, domainMapper);
    }

    @Test
    void buildFullEntityGraph_withNoExistingEntity_createsNewEntity() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(
                new CommonId("loc-1"),
                new CommonId("channel-1"));
        LocationEntity location = locationEntity(10L, "loc-1");

        ChannelFulfillmentRouteEntity result = assembler.buildFullEntityGraph(route, null, location);

        assertNotNull(result);
        assertEquals(10L, result.getLocationId());
        verify(entityMapper).toEntity(eq(route), any(ChannelFulfillmentRouteEntity.class));
    }

    @Test
    void buildFullEntityGraph_withExistingEntity_mergesIntoExistingEntity() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(
                new CommonId("loc-1"),
                new CommonId("channel-1"));
        LocationEntity location = locationEntity(10L, "loc-1");
        ChannelFulfillmentRouteEntity existingEntity = new ChannelFulfillmentRouteEntity();
        existingEntity.setLocationId(10L);
        existingEntity.setSalesChannelId("channel-1");

        ChannelFulfillmentRouteEntity result = assembler.buildFullEntityGraph(route, existingEntity, location);

        assertSame(existingEntity, result);
        verify(entityMapper).toEntity(same(route), same(existingEntity));
    }

    @Test
    void toFullDomainGraph_mapsEntityToDomain() {
        ChannelFulfillmentRouteEntity entity = new ChannelFulfillmentRouteEntity();
        entity.setLocationId(10L);
        entity.setSalesChannelId("channel-1");
        LocationEntity location = locationEntity(10L, "loc-1");
        ChannelFulfillmentRoute expected = ChannelFulfillmentRoute.restore(
                new CommonId("loc-1"),
                new CommonId("channel-1"));
        when(domainMapper.toDomain(entity, location)).thenReturn(expected);

        ChannelFulfillmentRoute result = assembler.toFullDomainGraph(entity, location);

        assertSame(expected, result);
        verify(domainMapper).toDomain(entity, location);
    }

    private static LocationEntity locationEntity(Long id, String uuid) {
        LocationEntity location = new LocationEntity();
        location.setId(id);
        location.setUuid(uuid);
        return location;
    }
}
