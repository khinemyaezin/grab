package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.entity.ZoneEntity;
import com.inventory.infrastructure.mapper.jpa.ZoneJpaAssembler;
import com.inventory.infrastructure.repository.jpa.ZoneJpaRepository;
import com.inventory.infrastructure.view.ZoneView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefaultZoneRepositoryTest {

    private static final String ZONE_RESOURCE = "Zone";

    private ZoneJpaRepository jpaRepository;
    private ZoneJpaAssembler mapper;
    private DomainEventProducer domainEventProducer;
    private PersistenceExecutor executor;
    private DefaultZoneRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(ZoneJpaRepository.class);
        mapper = mock(ZoneJpaAssembler.class);
        domainEventProducer = mock(DomainEventProducer.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultZoneRepository(jpaRepository, mapper, domainEventProducer, executor);

        when(executor.query(eq(ZONE_RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(executor.command(eq(ZONE_RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void findById_existingId_shouldDelegateToJpaRepositoryAndMapper() {
        ZoneEntity entity = createEntity("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        Zone zone = createDomain("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        when(jpaRepository.findByUuid("zone-1")).thenReturn(Optional.of(entity));
        when(mapper.toFullDomainGraph(entity)).thenReturn(zone);

        Optional<Zone> result = repository.findById(id("zone-1"));

        assertTrue(result.isPresent());
        assertSame(zone, result.get());
        verify(jpaRepository).findByUuid("zone-1");
        verify(mapper).toFullDomainGraph(entity);
        verify(executor).query(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void findById_nonExistentId_shouldReturnEmpty() {
        when(jpaRepository.findByUuid("non-existent")).thenReturn(Optional.empty());

        Optional<Zone> result = repository.findById(id("non-existent"));

        assertTrue(result.isEmpty());
        verify(jpaRepository).findByUuid("non-existent");
        verifyNoInteractions(mapper);
        verify(executor).query(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void queryByLocationId_validLocationId_shouldDelegateToJpaRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        ZoneView view = new ZoneView("zone-1", "ZONE-P1", "Picking Zone A", ZoneType.PICKING, true, "loc-1");
        Page<ZoneView> expectedPage = new PageImpl<>(List.of(view));
        when(jpaRepository.findAllByLocationId("loc-1", pageable)).thenReturn(expectedPage);

        Page<ZoneView> result = repository.queryByLocationId("loc-1", pageable);

        assertSame(expectedPage, result);
        verify(jpaRepository).findAllByLocationId("loc-1", pageable);
        verify(executor).query(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void queryByLocationIdAndActive_validLocationId_shouldDelegateToJpaRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        ZoneView view = new ZoneView("zone-1", "ZONE-P1", "Picking Zone A", ZoneType.PICKING, true, "loc-1");
        Page<ZoneView> expectedPage = new PageImpl<>(List.of(view));
        when(jpaRepository.findAllByLocationIdAndActive("loc-1", true, pageable)).thenReturn(expectedPage);

        Page<ZoneView> result = repository.queryByLocationIdAndActive("loc-1", true, pageable);

        assertSame(expectedPage, result);
        verify(jpaRepository).findAllByLocationIdAndActive("loc-1", true, pageable);
        verify(executor).query(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void save_newZone_shouldPersistAndPublishEvents() {
        Zone zone = Zone.create(id("zone-1"), id("loc-1"), "ZONE-P1", "Picking Zone A", ZoneType.PICKING);
        ZoneEntity newEntity = createEntity("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        ZoneEntity savedEntity = createEntity("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        savedEntity.setId(1L);
        Zone savedZone = createDomain("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);

        when(jpaRepository.findByUuid("zone-1")).thenReturn(Optional.empty());
        when(mapper.buildFullEntityGraph(same(zone), isNull())).thenReturn(newEntity);
        when(jpaRepository.save(newEntity)).thenReturn(savedEntity);
        when(mapper.toFullDomainGraph(savedEntity)).thenReturn(savedZone);

        Zone result = repository.save(zone);

        assertSame(savedZone, result);
        verify(jpaRepository).findByUuid("zone-1");
        verify(mapper).buildFullEntityGraph(same(zone), isNull());
        verify(jpaRepository).save(newEntity);
        verify(mapper).toFullDomainGraph(savedEntity);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eq("Zone"), eq("zone-1"), eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());

        verify(executor).command(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void save_existingZone_shouldMergeAndPublishEvents() {
        Zone zone = Zone.create(id("zone-1"), id("loc-1"), "ZONE-P1", "Picking Zone A", ZoneType.PICKING);
        ZoneEntity existingEntity = createEntity("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        existingEntity.setId(1L);
        ZoneEntity mergedEntity = createEntity("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        mergedEntity.setId(1L);
        ZoneEntity savedEntity = createEntity("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        savedEntity.setId(1L);
        Zone savedZone = createDomain("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);

        when(jpaRepository.findByUuid("zone-1")).thenReturn(Optional.of(existingEntity));
        when(mapper.buildFullEntityGraph(same(zone), same(existingEntity))).thenReturn(mergedEntity);
        when(jpaRepository.save(mergedEntity)).thenReturn(savedEntity);
        when(mapper.toFullDomainGraph(savedEntity)).thenReturn(savedZone);

        Zone result = repository.save(zone);

        assertSame(savedZone, result);
        verify(jpaRepository).findByUuid("zone-1");
        verify(mapper).buildFullEntityGraph(same(zone), same(existingEntity));
        verify(jpaRepository).save(mergedEntity);
        verify(mapper).toFullDomainGraph(savedEntity);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eq("Zone"), eq("zone-1"), eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());

        verify(executor).command(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void delete_existingId_shouldRemoveEntity() {
        ZoneEntity entity = createEntity("zone-1", "ZONE-P1", "loc-1", ZoneType.PICKING, true);
        when(jpaRepository.findByUuid("zone-1")).thenReturn(Optional.of(entity));

        repository.delete(id("zone-1"));

        verify(jpaRepository).findByUuid("zone-1");
        verify(jpaRepository).delete(entity);
        verify(executor).command(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void delete_nonExistentId_shouldDoNothing() {
        when(jpaRepository.findByUuid("non-existent")).thenReturn(Optional.empty());

        repository.delete(id("non-existent"));

        verify(jpaRepository).findByUuid("non-existent");
        verify(jpaRepository, never()).delete(any());
        verify(executor).command(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void existsByCodeAndLocationId_existingCodeAndLocation_shouldReturnTrue() {
        when(jpaRepository.existsByCodeAndLocationId("ZONE-P1", "loc-1")).thenReturn(true);

        boolean result = repository.existsByCodeAndLocationId("ZONE-P1", id("loc-1"));

        assertTrue(result);
        verify(jpaRepository).existsByCodeAndLocationId("ZONE-P1", "loc-1");
        verifyNoInteractions(mapper, domainEventProducer);
        verify(executor).query(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    @Test
    void existsByCodeAndLocationId_nonExistentCodeAndLocation_shouldReturnFalse() {
        when(jpaRepository.existsByCodeAndLocationId("INVALID", "loc-1")).thenReturn(false);

        boolean result = repository.existsByCodeAndLocationId("INVALID", id("loc-1"));

        assertFalse(result);
        verify(jpaRepository).existsByCodeAndLocationId("INVALID", "loc-1");
        verifyNoInteractions(mapper, domainEventProducer);
        verify(executor).query(eq(ZONE_RESOURCE), any(Supplier.class));
    }

    private static Id id(String value) {
        return () -> value;
    }

    private static ZoneEntity createEntity(String uuid, String code, String locationId, ZoneType type, boolean active) {
        ZoneEntity entity = new ZoneEntity();
        entity.setUuid(uuid);
        entity.setCode(code);
        entity.setName(code + " Name");
        entity.setType(type);
        entity.setLocationId(locationId);
        entity.setActive(active);
        return entity;
    }

    private static Zone createDomain(String uuid, String code, String locationId, ZoneType type, boolean active) {
        return new Zone(id(uuid), id(locationId), code, code + " Name", type, active);
    }
}
