package com.inventory.infrastructure.repository.jpa.impl;

import com.grab.framework.support.PersistenceExecutor;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.repository.jpa.InventoryItemJpaRepository;
import com.inventory.infrastructure.specification.jpa.InventorySearchCriteria;
import com.inventory.infrastructure.specification.jpa.InventorySearchSpecification;
import com.inventory.infrastructure.view.InventoryExistenceView;
import com.inventory.infrastructure.view.InventoryItemView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DefaultInventoryQueryRepositoryTest {

    private static final String INVENTORY_ITEM_RESOURCE = "InventoryItem";

    private InventorySearchSpecification searchSpecification;
    private InventoryItemJpaRepository jpaRepository;
    private PersistenceExecutor executor;
    private DefaultInventoryQueryRepository repository;

    @BeforeEach
    void setUp() {
        searchSpecification = mock(InventorySearchSpecification.class);
        jpaRepository = mock(InventoryItemJpaRepository.class);
        executor = mock(PersistenceExecutor.class);
        repository = new DefaultInventoryQueryRepository(searchSpecification, jpaRepository, executor);

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
}
