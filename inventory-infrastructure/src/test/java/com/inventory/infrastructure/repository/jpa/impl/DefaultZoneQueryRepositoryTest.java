package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.ZoneType;
import com.inventory.infrastructure.repository.jpa.ZoneJpaRepository;
import com.inventory.infrastructure.view.ZoneView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultZoneQueryRepositoryTest {

    private static final String ZONE_RESOURCE = "Zone";

    private ZoneJpaRepository jpaRepository;
    private PersistenceExecutor executor;
    private DefaultZoneQueryRepository repository;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(ZoneJpaRepository.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultZoneQueryRepository(jpaRepository, executor);

        when(executor.query(eq(ZONE_RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
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
}
