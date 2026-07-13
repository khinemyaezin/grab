package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.domain.Event;
import com.grab.framework.event.DomainEventProducer;
import com.grab.framework.id.Id;
import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.enums.LocationType;
import com.inventory.domain.valueobject.Address;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.mapper.jpa.LocationJpaAssembler;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.view.LocationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefaultLocationRepositoryTest {

    private static final String LOCATION_RESOURCE = "Location";

    private LocationJpaRepository jpaRepository;
    private LocationJpaAssembler mapper;
    private DomainEventProducer domainEventProducer;
    private PersistenceExecutor executor;
    private DefaultLocationRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(LocationJpaRepository.class);
        mapper = mock(LocationJpaAssembler.class);
        domainEventProducer = mock(DomainEventProducer.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultLocationRepository(jpaRepository, mapper, domainEventProducer, executor);

        when(executor.query(eq(LOCATION_RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
        when(executor.command(eq(LOCATION_RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void findById_returnsLocation_whenExists() {
        LocationEntity entity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        Location location = createLocation("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        when(jpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(entity));
        when(mapper.toFullDomainGraph(entity)).thenReturn(location);

        Optional<Location> result = repository.findById(id("loc-1"));

        assertTrue(result.isPresent());
        assertSame(location, result.get());
        verify(jpaRepository).findByUuid("loc-1");
        verify(mapper).toFullDomainGraph(entity);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        when(jpaRepository.findByUuid("non-existent")).thenReturn(Optional.empty());

        Optional<Location> result = repository.findById(id("non-existent"));

        assertTrue(result.isEmpty());
        verify(jpaRepository).findByUuid("non-existent");
        verifyNoInteractions(mapper);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void findByCode_returnsLocation_whenExists() {
        LocationEntity entity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        Location location = createLocation("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        when(jpaRepository.findByCode("WH-001")).thenReturn(Optional.of(entity));
        when(mapper.toFullDomainGraph(entity)).thenReturn(location);

        Optional<Location> result = repository.findByCode("WH-001");

        assertTrue(result.isPresent());
        assertSame(location, result.get());
        verify(jpaRepository).findByCode("WH-001");
        verify(mapper).toFullDomainGraph(entity);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void findByCode_returnsEmpty_whenNotExists() {
        when(jpaRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        Optional<Location> result = repository.findByCode("INVALID");

        assertTrue(result.isEmpty());
        verify(jpaRepository).findByCode("INVALID");
        verifyNoInteractions(mapper);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void existsByCode_returnsTrue_whenExists() {
        when(jpaRepository.existsByCode("WH-001")).thenReturn(true);

        boolean result = repository.existsByCode("WH-001");

        assertTrue(result);
        verify(jpaRepository).existsByCode("WH-001");
        verifyNoInteractions(mapper, domainEventProducer);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void existsByCode_returnsFalse_whenNotExists() {
        when(jpaRepository.existsByCode("INVALID")).thenReturn(false);

        boolean result = repository.existsByCode("INVALID");

        assertFalse(result);
        verify(jpaRepository).existsByCode("INVALID");
        verifyNoInteractions(mapper, domainEventProducer);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }


    @Test
    void save_createsNewEntity_whenNotExisting() {
        Location location = Location.create(id("loc-1"), id("seller-1"), "WH-001", "Main Warehouse", LocationType.WAREHOUSE, address());
        LocationEntity newEntity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        LocationEntity savedEntity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        savedEntity.setId(1L);
        Location savedLocation = createLocation("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);

        when(jpaRepository.findByUuid("loc-1")).thenReturn(Optional.empty());
        when(mapper.buildFullEntityGraph(same(location), isNull())).thenReturn(newEntity);
        when(jpaRepository.save(newEntity)).thenReturn(savedEntity);
        when(mapper.toFullDomainGraph(savedEntity)).thenReturn(savedLocation);

        Location result = repository.save(location);

        assertSame(savedLocation, result);
        verify(jpaRepository).findByUuid("loc-1");
        verify(mapper).buildFullEntityGraph(same(location), isNull());
        verify(jpaRepository).save(newEntity);
        verify(mapper).toFullDomainGraph(savedEntity);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eq("Location"), eq("loc-1"), eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());

        verify(executor).command(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void save_updatesExistingEntity_whenAlreadyExists() {
        Location location = Location.create(id("loc-1"), id("seller-1"), "WH-001", "Main Warehouse", LocationType.WAREHOUSE, address());
        LocationEntity existingEntity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        existingEntity.setId(1L);
        LocationEntity mergedEntity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        mergedEntity.setId(1L);
        LocationEntity savedEntity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        savedEntity.setId(1L);
        Location savedLocation = createLocation("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);

        when(jpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(existingEntity));
        when(mapper.buildFullEntityGraph(same(location), same(existingEntity))).thenReturn(mergedEntity);
        when(jpaRepository.save(mergedEntity)).thenReturn(savedEntity);
        when(mapper.toFullDomainGraph(savedEntity)).thenReturn(savedLocation);

        Location result = repository.save(location);

        assertSame(savedLocation, result);
        verify(jpaRepository).findByUuid("loc-1");
        verify(mapper).buildFullEntityGraph(same(location), same(existingEntity));
        verify(jpaRepository).save(mergedEntity);
        verify(mapper).toFullDomainGraph(savedEntity);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Event>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventProducer).produce(eq("Location"), eq("loc-1"), eventsCaptor.capture());
        assertFalse(eventsCaptor.getValue().isEmpty());

        verify(executor).command(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void delete_removesExistingEntity_whenFound() {
        LocationEntity entity = createLocationEntity("loc-1", "WH-001", "seller-1", LocationType.WAREHOUSE, true);
        when(jpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(entity));

        repository.delete(id("loc-1"));

        verify(jpaRepository).findByUuid("loc-1");
        verify(jpaRepository).delete(entity);
        verify(executor).command(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void delete_doesNothing_whenNotExisting() {
        when(jpaRepository.findByUuid("non-existent")).thenReturn(Optional.empty());

        repository.delete(id("non-existent"));

        verify(jpaRepository).findByUuid("non-existent");
        verify(jpaRepository, never()).delete(any());
        verify(executor).command(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    private static Id id(String value) {
        return new Id() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Id other)) return false;
                return Objects.equals(value, other.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(value);
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }

    private static Address address() {
        return new Address("123 Main St", null, "Springfield", "IL", "62701", "US");
    }

    private static LocationEntity createLocationEntity(String uuid, String code, String merchantId, LocationType type, boolean active) {
        LocationEntity entity = new LocationEntity();
        entity.setUuid(uuid);
        entity.setCode(code);
        entity.setName(code + " Name");
        entity.setMerchantId(merchantId);
        entity.setType(type);
        entity.setStreet("123 Main St");
        entity.setCity("Springfield");
        entity.setState("IL");
        entity.setPostalCode("62701");
        entity.setCountry("US");
        entity.setActive(active);
        return entity;
    }

    private static Location createLocation(String uuid, String code, String merchantId, LocationType type, boolean active) {
        return new Location(id(uuid), id(merchantId), code, code + " Name", type, address(), active);
    }

    private static LocationView locationView(String uuid, String code) {
        return new LocationView(uuid, code, code + " Name", LocationType.WAREHOUSE,
                "123 Main St", null, "Springfield", "IL", "62701", "US", true);
    }
}
