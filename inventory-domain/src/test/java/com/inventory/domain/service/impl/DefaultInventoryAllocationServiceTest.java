package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.AllocationError;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.service.InventoryAllocationService.AllocationResult;
import com.inventory.domain.valueobject.InventoryQuantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultInventoryAllocationServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private IdGenerator idGenerator;

    private DefaultInventoryAllocationService allocationService;

    @BeforeEach
    void setUp() {
        allocationService = new DefaultInventoryAllocationService(inventoryRepository, idGenerator);
       // when(idGenerator.generateId()).thenReturn(id("movement-1"));
    }

    @Test
    void allocateStock_emptyInventoryItem_returnNoInventoryFound() {
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of());

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isEqualTo(AllocationError.NO_AVAILABLE_INVENTORY);
        assertThat(result.allocatedQuantity()).isZero();
    }

    @Test
    void allocateStock_withInactiveInventoryExists_returnFailure() {
        InventoryItem inactiveItem = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.SUSPENDED);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(inactiveItem));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isEqualTo(AllocationError.NO_AVAILABLE_INVENTORY);
    }

    @Test
    void allocateStock_withZeroQuantityInventory_returnFailure() {
        InventoryItem emptyItem = createInventoryItem("item-1", "SKU-001", "loc-1", 0, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(emptyItem));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isEqualTo(AllocationError.NO_AVAILABLE_INVENTORY);
    }

    @Test
    void allocateStock_withInsufficientTotalStock_returnFailure() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 3, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isEqualTo(AllocationError.INSUFFICIENT_STOCK);
        assertThat(result.errorMessage()).isEqualTo("Insufficient stock. Available: 8, Requested: 10");
        assertThat(result.allocatedQuantity()).isZero();
    }

    @Test
    void allocateStock_withSingleItem_shouldSuccessfullyAllocate() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isTrue();
        assertThat(result.allocatedQuantity()).isEqualTo(10);
        assertThat(result.requestedQuantity()).isEqualTo(10);
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().getFirst().quantity()).isEqualTo(10);
        assertThat(result.allocations().getFirst().inventoryItemId().getValue()).isEqualTo("item-1");

        verify(inventoryRepository).save(item);
        assertThat(item.getQuantity().reserved()).isEqualTo(10);
    }

    @Test
    void allocateStock_withExactAvailableQty_shouldAllocate() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 10, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isTrue();
        assertThat(result.allocatedQuantity()).isEqualTo(10);
        assertThat(item.getAvailableQuantity()).isZero();
    }

    @Test
    void allocateStock_withMultipleInventoryItems_shouldAllocate() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 10, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        AllocationResult result = allocationService.allocateStock("SKU-001", 12, "order-1");

        assertThat(result.success()).isTrue();
        assertThat(result.allocatedQuantity()).isEqualTo(12);
        assertThat(result.allocations()).hasSize(2);

        verify(inventoryRepository, times(2)).save(any(InventoryItem.class));
    }

    @Test
    void allocateStock_withMultipleInventoryItem_shouldPrioritizeHighestAvailableQuantity() {
        InventoryItem smallItem = createInventoryItem("small", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        InventoryItem largeItem = createInventoryItem("large", "SKU-001", "loc-2", 100, InventoryStatus.ACTIVE);
        InventoryItem mediumItem = createInventoryItem("medium", "SKU-001", "loc-3", 50, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(smallItem, largeItem, mediumItem));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isTrue();
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().getFirst().inventoryItemId().getValue()).isEqualTo("large");
        assertThat(result.allocations().getFirst().quantity()).isEqualTo(10);
    }

    @Test
    void allocateStock_withInactiveItems_shouldSkip() {
        InventoryItem activeItem = createInventoryItem("active", "SKU-001", "loc-1", 10, InventoryStatus.ACTIVE);
        InventoryItem suspendedItem = createInventoryItem("suspended", "SKU-001", "loc-2", 100, InventoryStatus.SUSPENDED);
        InventoryItem discontinuedItem = createInventoryItem("discontinued", "SKU-001", "loc-3", 100, InventoryStatus.DISCONTINUED);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(activeItem, suspendedItem, discontinuedItem));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1");

        assertThat(result.success()).isTrue();
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().getFirst().inventoryItemId().getValue()).isEqualTo("active");

        verify(inventoryRepository, times(1)).save(activeItem);
    }

    private InventoryItem createInventoryItem(String itemId, String sku, String locationId, int onHand, InventoryStatus status) {
        return new InventoryItem(
                id(itemId),
                sku,
                id("variant-1"),
                id(locationId),
                InventoryQuantity.withOnHand(onHand),
                null,
                status
        );
    }

    private Id id(String value) {
        return () -> value;
    }
}