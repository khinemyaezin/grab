package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.LocationEntity;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.repository.jpa.LocationJpaRepository;
import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.specification.jpa.InventorySearchSpecification;
import com.inventory.infrastructure.specification.jpa.InventorySummarySpecification;
import com.inventory.infrastructure.view.InventoryExistenceView;
import com.inventory.infrastructure.view.InventoryItemView;
import com.inventory.infrastructure.view.InventorySummaryAggregationView;
import com.inventory.infrastructure.view.InventorySummaryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultInventoryQueryRepositoryTest {

    private static final String INVENTORY_ITEM_RESOURCE = "InventoryItem";

    private InventorySearchSpecification searchSpecification;
    private InventorySummarySpecification summarySpecification;
    private InventoryItemJpaRepository jpaRepository;
    private LocationJpaRepository locationJpaRepository;
    private PersistenceExecutor executor;
    private DefaultInventoryQueryRepository repository;

    @BeforeEach
    void setUp() {
        searchSpecification = mock(InventorySearchSpecification.class);
        summarySpecification = mock(InventorySummarySpecification.class);
        jpaRepository = mock(InventoryItemJpaRepository.class);
        locationJpaRepository = mock(LocationJpaRepository.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultInventoryQueryRepository(
                searchSpecification,
                summarySpecification,
                jpaRepository,
                locationJpaRepository,
                executor
        );

        when(executor.query(eq(INVENTORY_ITEM_RESOURCE), any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });
    }

    @Test
    void search_withCriteria_shouldDelegateToSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        InventorySearchCriteria criteria = new InventorySearchCriteria("seller-1", "SKU", "loc-1", InventoryStatus.ACTIVE);
        InventoryItemView view = new InventoryItemView(
                "uuid-inv-1", "SKU-001", "seller-1", "variant-1", "loc-1", "LOC-1", "Warehouse One",
                100, 10, 0, 0, 20, 30, 50, 200,
                InventoryStatus.ACTIVE, LocalDateTime.now()
        );
        Page<InventoryItemView> expectedPage = new PageImpl<>(List.of(view));
        when(searchSpecification.search(criteria, pageable)).thenReturn(expectedPage);

        Page<InventoryItemView> result = repository.search(criteria, pageable);

        assertSame(expectedPage, result);
        verify(searchSpecification).search(criteria, pageable);
        verify(executor).query(eq(INVENTORY_ITEM_RESOURCE), any(Supplier.class));
    }

    @Test
    void findExistenceByMerchantLocationAndSkus_shouldMapEntitiesToViews() {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setUuid("uuid-inv-1");
        entity.setSku("SKU-001");
        when(jpaRepository.findAllByMerchantIdAndLocationIdAndSkuIn(
                "seller-1", "loc-1", List.of("SKU-001", "SKU-002")))
                .thenReturn(List.of(entity));

        List<InventoryExistenceView> result = repository.findExistenceByMerchantLocationAndSkus(
                "seller-1", "loc-1", List.of("SKU-001", "SKU-002"));

        assertThat(result).containsExactly(new InventoryExistenceView("uuid-inv-1", "SKU-001"));
        verify(jpaRepository).findAllByMerchantIdAndLocationIdAndSkuIn(
                "seller-1", "loc-1", List.of("SKU-001", "SKU-002"));
        verify(executor).query(eq(INVENTORY_ITEM_RESOURCE), any(Supplier.class));
    }

    @Test
    void summarize_withLocation_shouldResolveLocationAttributes() {
        InventorySummaryAggregationView aggregation = new InventorySummaryAggregationView(
                5, 3, 1, 1, 0, 4, 1, 1, 1, 1, 265, 30, 0, 0, 235
        );
        when(summarySpecification.aggregate("seller-1", "loc-1")).thenReturn(aggregation);
        LocationEntity location = new LocationEntity();
        location.setUuid("loc-1");
        location.setCode("LOC-1");
        location.setName("Warehouse One");
        when(locationJpaRepository.findByUuid("loc-1")).thenReturn(Optional.of(location));

        InventorySummaryView result = repository.summarize("seller-1", "loc-1");

        assertThat(result.totalItems()).isEqualTo(5);
        assertThat(result.scope().locationId()).isEqualTo("loc-1");
        assertThat(result.scope().locationCode()).isEqualTo("LOC-1");
        assertThat(result.scope().locationName()).isEqualTo("Warehouse One");
        assertThat(result.health().eligibleItems()).isEqualTo(4);
        assertThat(result.quantities().available()).isEqualTo(235);
    }

    @Test
    void summarize_withoutLocation_shouldLeaveLocationAttributesNull() {
        InventorySummaryAggregationView aggregation = new InventorySummaryAggregationView(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        );
        when(summarySpecification.aggregate("seller-1", null)).thenReturn(aggregation);

        InventorySummaryView result = repository.summarize("seller-1", null);

        assertThat(result.scope().merchantId()).isEqualTo("seller-1");
        assertThat(result.scope().locationId()).isNull();
        assertThat(result.scope().locationCode()).isNull();
        assertThat(result.scope().locationName()).isNull();
        verifyNoInteractions(locationJpaRepository);
    }
}
