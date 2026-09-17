package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.ChannelFulfillmentRoute;
import com.inventory.infrastructure.entity.ChannelFulfillmentRouteEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.ChannelFulfillmentRouteJpaAssembler;
import com.inventory.infrastructure.repository.jpa.ChannelFulfillmentRouteJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultChannelFulfillmentRouteRepositoryTest {

    private static final String RESOURCE = "ChannelFulfillmentRoute";

    private ChannelFulfillmentRouteJpaRepository jpaRepository;
    private LocationJpaRepository locationJpaRepository;
    private ChannelFulfillmentRouteJpaAssembler mapper;
    private DomainEventProducer domainEventProducer;
    private PersistenceExecutor executor;
    private DefaultChannelFulfillmentRouteRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(ChannelFulfillmentRouteJpaRepository.class);
        locationJpaRepository = mock(LocationJpaRepository.class);
        mapper = mock(ChannelFulfillmentRouteJpaAssembler.class);
        domainEventProducer = mock(DomainEventProducer.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultChannelFulfillmentRouteRepository(
                jpaRepository,
                locationJpaRepository,
                mapper,
                domainEventProducer,
                executor
        );

        when(executor.query(eq(RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(executor.command(eq(RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void find_returnsRoute_whenExists() {
        LocationEntity location = locationEntity(10L, "loc-1");
        ChannelFulfillmentRouteEntity entity = routeEntity(10L, "channel-1");
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(id("loc-1"), id("channel-1"));
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(location));
        when(jpaRepository.findByLocationIdAndSalesChannelId(10L, "channel-1")).thenReturn(Optional.of(entity));
        when(mapper.toFullDomainGraph(entity, location)).thenReturn(route);

        Optional<ChannelFulfillmentRoute> result = repository.find(id("loc-1"), id("channel-1"));

        assertTrue(result.isPresent());
        assertSame(route, result.get());
        verify(mapper).toFullDomainGraph(entity, location);
        verify(executor).query(eq(RESOURCE), any(Supplier.class));
    }

    @Test
    void find_returnsEmpty_whenLocationMissing() {
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.empty());

        Optional<ChannelFulfillmentRoute> result = repository.find(id("loc-1"), id("channel-1"));

        assertTrue(result.isEmpty());
        verifyNoInteractions(jpaRepository, mapper);
    }

    @Test
    void findByLocationId_mapsEntitiesThroughAssembler() {
        LocationEntity location = locationEntity(10L, "loc-1");
        ChannelFulfillmentRouteEntity entity = routeEntity(10L, "channel-1");
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(id("loc-1"), id("channel-1"));
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(location));
        when(jpaRepository.findByLocationId(10L)).thenReturn(List.of(entity));
        when(mapper.toFullDomainGraph(entity, location)).thenReturn(route);

        List<ChannelFulfillmentRoute> result = repository.findByLocationId(id("loc-1"));

        assertEquals(1, result.size());
        assertSame(route, result.get(0));
        verify(mapper).toFullDomainGraph(entity, location);
    }

    @Test
    void save_createsNewEntity_whenNotExisting() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.link(id("loc-1"), id("channel-1"));
        LocationEntity location = locationEntity(10L, "loc-1");
        ChannelFulfillmentRouteEntity newEntity = routeEntity(10L, "channel-1");
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(location));
        when(jpaRepository.findByLocationIdAndSalesChannelId(10L, "channel-1")).thenReturn(Optional.empty());
        when(mapper.buildFullEntityGraph(same(route), isNull(), same(location))).thenReturn(newEntity);

        repository.save(route);

        verify(mapper).buildFullEntityGraph(same(route), isNull(), same(location));
        verify(jpaRepository).save(newEntity);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eq("ChannelFulfillmentRoute"), eq("loc-1:channel-1"), eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());
    }

    @Test
    void save_updatesExistingEntity_whenAlreadyExists() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.link(id("loc-1"), id("channel-1"));
        LocationEntity location = locationEntity(10L, "loc-1");
        ChannelFulfillmentRouteEntity existingEntity = routeEntity(10L, "channel-1");
        ChannelFulfillmentRouteEntity mergedEntity = routeEntity(10L, "channel-1");
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(location));
        when(jpaRepository.findByLocationIdAndSalesChannelId(10L, "channel-1")).thenReturn(Optional.of(existingEntity));
        when(mapper.buildFullEntityGraph(same(route), same(existingEntity), same(location))).thenReturn(mergedEntity);

        repository.save(route);

        verify(mapper).buildFullEntityGraph(same(route), same(existingEntity), same(location));
        verify(jpaRepository).save(mergedEntity);
        verify(domainEventProducer).produce(eq("ChannelFulfillmentRoute"), eq("loc-1:channel-1"), any());
    }

    @Test
    void delete_removesExistingEntity_whenFound() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(id("loc-1"), id("channel-1"));
        route.unlink();
        LocationEntity location = locationEntity(10L, "loc-1");
        ChannelFulfillmentRouteEntity entity = routeEntity(10L, "channel-1");
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(location));
        when(jpaRepository.findByLocationIdAndSalesChannelId(10L, "channel-1")).thenReturn(Optional.of(entity));

        repository.delete(route);

        verify(jpaRepository).delete(entity);
        verifyNoInteractions(mapper);
        verify(domainEventProducer).produce(eq("ChannelFulfillmentRoute"), eq("loc-1:channel-1"), any());
    }

    @Test
    void delete_doesNothing_whenNotExisting() {
        ChannelFulfillmentRoute route = ChannelFulfillmentRoute.restore(id("loc-1"), id("channel-1"));
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.empty());

        repository.delete(route);

        verify(jpaRepository, never()).delete(any());
        verifyNoInteractions(mapper);
        verify(domainEventProducer).produce(eq("ChannelFulfillmentRoute"), eq("loc-1:channel-1"), any());
    }

    private static Id id(String value) {
        return new CommonId(value);
    }

    private static LocationEntity locationEntity(Long id, String uuid) {
        LocationEntity location = new LocationEntity();
        location.setId(id);
        location.setUuid(uuid);
        return location;
    }

    private static ChannelFulfillmentRouteEntity routeEntity(Long locationId, String salesChannelId) {
        ChannelFulfillmentRouteEntity entity = new ChannelFulfillmentRouteEntity();
        entity.setLocationId(locationId);
        entity.setSalesChannelId(salesChannelId);
        return entity;
    }
}
