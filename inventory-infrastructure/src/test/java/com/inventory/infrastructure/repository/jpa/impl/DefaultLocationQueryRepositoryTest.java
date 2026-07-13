package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.LocationType;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.view.LocationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultLocationQueryRepositoryTest {

    private static final String LOCATION_RESOURCE = "Location";

    private LocationJpaRepository jpaRepository;
    private PersistenceExecutor executor;
    private DefaultLocationQueryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(LocationJpaRepository.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultLocationQueryRepository(jpaRepository, executor);

        when(executor.query(eq(LOCATION_RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void queryAll_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        LocationView view = locationView("loc-1", "WH-001");
        Page<LocationView> expectedPage = new PageImpl<>(List.of(view));
        when(jpaRepository.findAllBySellerId("seller-1", pageable)).thenReturn(expectedPage);

        Page<LocationView> result = repository.queryAll("seller-1", pageable);

        assertSame(expectedPage, result);
        assertEquals(1, result.getContent().size());
        verify(jpaRepository).findAllBySellerId("seller-1", pageable);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void queryByActive_returnsOnlyActiveLocations() {
        Pageable pageable = PageRequest.of(0, 10);
        LocationView view = locationView("loc-1", "WH-001");
        Page<LocationView> expectedPage = new PageImpl<>(List.of(view));
        when(jpaRepository.findAllBySellerIdAndActiveTrue("seller-1", pageable)).thenReturn(expectedPage);

        Page<LocationView> result = repository.queryByActive("seller-1", pageable);

        assertSame(expectedPage, result);
        verify(jpaRepository).findAllBySellerIdAndActiveTrue("seller-1", pageable);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    @Test
    void queryByType_returnsLocationsByType() {
        Pageable pageable = PageRequest.of(0, 10);
        LocationView view = locationView("loc-1", "WH-001");
        Page<LocationView> expectedPage = new PageImpl<>(List.of(view));
        when(jpaRepository.findAllBySellerIdAndType("seller-1", LocationType.WAREHOUSE, pageable)).thenReturn(expectedPage);

        Page<LocationView> result = repository.queryByType("seller-1", LocationType.WAREHOUSE, pageable);

        assertSame(expectedPage, result);
        verify(jpaRepository).findAllBySellerIdAndType("seller-1", LocationType.WAREHOUSE, pageable);
        verify(executor).query(eq(LOCATION_RESOURCE), any(Supplier.class));
    }

    private static LocationView locationView(String uuid, String code) {
        return new LocationView(uuid, code, code + " Name", LocationType.WAREHOUSE,
                "123 Main St", null, "Springfield", "IL", "62701", "US", true);
    }
}
