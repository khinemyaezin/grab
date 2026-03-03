package com.inventory.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.exception.AllocationError;
import com.inventory.domain.enums.InventoryStatus;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.StockMovementRepository;
import com.inventory.domain.service.InventoryAllocationService.AllocationResult;
import com.inventory.domain.valueobject.InventoryQuantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultInventoryAllocationServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private Id userId;

    private DefaultInventoryAllocationService allocationService;

    @BeforeEach
    void setUp() {
        allocationService = new DefaultInventoryAllocationService(inventoryRepository, stockMovementRepository, idGenerator);
    }

    @Test
    void allocateStock_emptyInventoryItem_returnNoInventoryFound() {
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of());
        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.NoAvailableInventory.class);
        AllocationError.NoAvailableInventory error = (AllocationError.NoAvailableInventory) result.error();
        assertThat(error.sku()).isEqualTo("SKU-001");
        assertThat(result.allocatedQuantity()).isZero();
    }

    @Test
    void allocateStock_withInactiveInventoryExists_returnFailure() {
        InventoryItem inactiveItem = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.SUSPENDED);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(inactiveItem));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", idGenerator.generateId());

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.NoAvailableInventory.class);
    }

    @Test
    void allocateStock_withZeroQuantityInventory_returnFailure() {
        InventoryItem emptyItem = createInventoryItem("item-1", "SKU-001", "loc-1", 0, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(emptyItem));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.NoAvailableInventory.class);
    }

    @Test
    void allocateStock_withInsufficientTotalStock_returnFailure() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 3, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.InsufficientStock.class);
        AllocationError.InsufficientStock error = (AllocationError.InsufficientStock) result.error();
        assertThat(error.available()).isEqualTo(8);
        assertThat(error.requested()).isEqualTo(10);
        assertThat(result.allocatedQuantity()).isZero();
    }

    @Test
    void allocateStock_withSingleItem_shouldSuccessfullyAllocate() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item));

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", userId);

        assertThat(result.success()).isTrue();
        assertThat(result.error()).isNull();
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

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", userId);

        assertThat(result.success()).isTrue();
        assertThat(result.allocatedQuantity()).isEqualTo(10);
        assertThat(item.getAvailableQuantity()).isZero();
    }

    @Test
    void allocateStock_withMultipleInventoryItems_shouldAllocate() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 10, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        AllocationResult result = allocationService.allocateStock("SKU-001", 12, "order-1", userId);

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

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", userId);

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

        AllocationResult result = allocationService.allocateStock("SKU-001", 10, "order-1", userId);

        assertThat(result.success()).isTrue();
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().getFirst().inventoryItemId().getValue()).isEqualTo("active");

        verify(inventoryRepository, times(1)).save(activeItem);
    }

    @Test
    void allocateStock_withNegativeQuantity_returnQuantityNotPositive() {
        AllocationResult result = allocationService.allocateStock("SKU-001", -5, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.QuantityNotPositive.class);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void allocateStock_withZeroQuantity_returnQuantityNotPositive() {
        AllocationResult result = allocationService.allocateStock("SKU-001", 0, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.QuantityNotPositive.class);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void allocateStockFromLocation_withSingleItem_shouldSuccessfullyAllocate() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySkuAndLocation("SKU-001", item.getLocationId())).thenReturn(Optional.of(item));

        AllocationResult result = allocationService.allocateStockFromLocation("SKU-001", item.getLocationId(), 10, "order-1", userId);

        assertThat(result.success()).isTrue();
        assertThat(result.error()).isNull();
        assertThat(result.allocatedQuantity()).isEqualTo(10);
        assertThat(result.requestedQuantity()).isEqualTo(10);
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().getFirst().quantity()).isEqualTo(10);
        assertThat(result.allocations().getFirst().inventoryItemId().getValue()).isEqualTo("item-1");

        verify(inventoryRepository).save(item);
        assertThat(item.getQuantity().reserved()).isEqualTo(10);
    }

    @Test
    void allocateStockFromLocation_emptyInventoryItem_returnNoInventoryFound() {
        Id locationId = id("loc-1");
        when(inventoryRepository.findBySkuAndLocation("SKU-001", locationId)).thenReturn(Optional.empty());
        AllocationResult result = allocationService.allocateStockFromLocation("SKU-001", locationId, 10, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.InventoryNotFoundAtLocation.class);
        AllocationError.InventoryNotFoundAtLocation error = (AllocationError.InventoryNotFoundAtLocation) result.error();
        assertThat(error.sku()).isEqualTo("SKU-001");
        assertThat(result.allocatedQuantity()).isZero();
    }

    @Test
    void allocateStockFromLocation_withInactiveItem_shouldReturnFailure() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.SUSPENDED);
        when(inventoryRepository.findBySkuAndLocation("SKU-001", item.getLocationId())).thenReturn(Optional.of(item));

        AllocationResult result = allocationService.allocateStockFromLocation("SKU-001", item.getLocationId(), 10, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.InventoryItemNotActive.class);
        AllocationError.InventoryItemNotActive error = (AllocationError.InventoryItemNotActive) result.error();
        assertThat(error.sku()).isEqualTo("SKU-001");
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void allocateStockFromLocation_withInsufficientStock_shouldReturnFailure() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySkuAndLocation("SKU-001", item.getLocationId())).thenReturn(Optional.of(item));

        AllocationResult result = allocationService.allocateStockFromLocation("SKU-001", item.getLocationId(), 10, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.InsufficientStock.class);
        AllocationError.InsufficientStock error = (AllocationError.InsufficientStock) result.error();
        assertThat(error.available()).isEqualTo(5);
        assertThat(error.requested()).isEqualTo(10);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void allocateStockFromLocation_withNegativeQuantity_shouldReturnFailure() {
        Id locationId = id("loc-1");
        AllocationResult result = allocationService.allocateStockFromLocation("SKU-001", locationId, -5, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.QuantityNotPositive.class);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void allocateStockFromLocation_withZeroQuantity_shouldReturnFailure() {
        Id locationId = id("loc-1");
        AllocationResult result = allocationService.allocateStockFromLocation("SKU-001", locationId, 0, "order-1", userId);

        assertThat(result.success()).isFalse();
        assertThat(result.error()).isInstanceOf(AllocationError.QuantityNotPositive.class);
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void allocateStockFromLocation_withExactAvailableQuantity_shouldSucceed() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 10, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySkuAndLocation("SKU-001", item.getLocationId())).thenReturn(Optional.of(item));

        AllocationResult result = allocationService.allocateStockFromLocation("SKU-001", item.getLocationId(), 10, "order-1", userId);

        assertThat(result.success()).isTrue();
        assertThat(result.allocatedQuantity()).isEqualTo(10);
        assertThat(item.getAvailableQuantity()).isZero();
        verify(inventoryRepository).save(item);
    }

    // ========== Tests for deallocateStock ==========

    @Test
    void deallocateStock_withSingleLocation_shouldReleaseReservation() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        item.reserveStock(20, "order-1", userId, id("mov-1"));
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item));

        allocationService.deallocateStock("SKU-001", 10, "order-1", userId);

        assertThat(item.getQuantity().reserved()).isEqualTo(10);
        verify(inventoryRepository).save(item);
        verify(stockMovementRepository).save(any());
    }

    @Test
    void deallocateStock_withMultipleLocations_shouldReleaseFromAll() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 100, InventoryStatus.ACTIVE);
        item1.reserveStock(10, "order-1", userId, id("mov-1"));
        item2.reserveStock(10, "order-1", userId, id("mov-2"));
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        allocationService.deallocateStock("SKU-001", 15, "order-1", userId);

        assertThat(item1.getQuantity().reserved()).isZero();
        assertThat(item2.getQuantity().reserved()).isEqualTo(5);
        verify(inventoryRepository, times(2)).save(any());
        verify(stockMovementRepository, times(2)).save(any());
    }

    @Test
    void deallocateStock_withExactReservedAmount_shouldReleaseAll() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        item.reserveStock(20, "order-1", userId, id("mov-1"));
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item));

        allocationService.deallocateStock("SKU-001", 20, "order-1", userId);

        assertThat(item.getQuantity().reserved()).isZero();
        verify(inventoryRepository).save(item);
    }

    @Test
    void deallocateStock_withZeroReserved_shouldNotSaveOrCreateMovement() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item));

        allocationService.deallocateStock("SKU-001", 10, "order-1", userId);

        verify(inventoryRepository, times(1)).findBySku("SKU-001");
        verify(inventoryRepository, never()).save(any());
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void deallocateStock_withPartialRelease_shouldReleaseOnlyRequested() {
        InventoryItem item = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        item.reserveStock(30, "order-1", userId, id("mov-1"));
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item));

        allocationService.deallocateStock("SKU-001", 10, "order-1", userId);

        assertThat(item.getQuantity().reserved()).isEqualTo(20);
        verify(inventoryRepository).save(item);
    }

    @Test
    void deallocateStock_withNoInventoryItems_shouldDoNothing() {
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of());

        allocationService.deallocateStock("SKU-001", 10, "order-1", userId);

        verify(inventoryRepository).findBySku("SKU-001");
        verify(inventoryRepository, never()).save(any());
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void canAllocate_withSufficientStock_shouldReturnTrue() {
        when(inventoryRepository.getTotalAvailableQuantityBySku("SKU-001")).thenReturn(100);

        boolean result = allocationService.canAllocate("SKU-001", 50);

        assertThat(result).isTrue();
    }

    @Test
    void canAllocate_withExactStock_shouldReturnTrue() {
        when(inventoryRepository.getTotalAvailableQuantityBySku("SKU-001")).thenReturn(50);

        boolean result = allocationService.canAllocate("SKU-001", 50);

        assertThat(result).isTrue();
    }

    @Test
    void canAllocate_withInsufficientStock_shouldReturnFalse() {
        when(inventoryRepository.getTotalAvailableQuantityBySku("SKU-001")).thenReturn(10);

        boolean result = allocationService.canAllocate("SKU-001", 50);

        assertThat(result).isFalse();
    }

    @Test
    void canAllocate_withZeroStock_shouldReturnFalse() {
        when(inventoryRepository.getTotalAvailableQuantityBySku("SKU-001")).thenReturn(0);

        boolean result = allocationService.canAllocate("SKU-001", 10);

        assertThat(result).isFalse();
    }

    @Test
    void canAllocate_withZeroQuantityRequested_shouldReturnTrue() {
        when(inventoryRepository.getTotalAvailableQuantityBySku("SKU-001")).thenReturn(100);

        boolean result = allocationService.canAllocate("SKU-001", 0);

        assertThat(result).isTrue();
    }

    @Test
    void getAvailableForAllocation_shouldReturnTotalAvailable() {
        when(inventoryRepository.getTotalAvailableQuantityBySku("SKU-001")).thenReturn(150);

        int result = allocationService.getAvailableForAllocation("SKU-001");

        assertThat(result).isEqualTo(150);
    }

    @Test
    void getAvailableForAllocation_withNoStock_shouldReturnZero() {
        when(inventoryRepository.getTotalAvailableQuantityBySku("SKU-001")).thenReturn(0);

        int result = allocationService.getAvailableForAllocation("SKU-001");

        assertThat(result).isZero();
    }

    @Test
    void findAvailableInventory_shouldReturnOnlyActiveItems() {
        InventoryItem activeItem = createInventoryItem("active", "SKU-001", "loc-1", 10, InventoryStatus.ACTIVE);
        InventoryItem suspendedItem = createInventoryItem("suspended", "SKU-001", "loc-2", 20, InventoryStatus.SUSPENDED);
        InventoryItem discontinuedItem = createInventoryItem("discontinued", "SKU-001", "loc-3", 30, InventoryStatus.DISCONTINUED);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(activeItem, suspendedItem, discontinuedItem));

        List<InventoryItem> result = allocationService.findAvailableInventory("SKU-001");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId().getValue()).isEqualTo("active");
    }

    @Test
    void findAvailableInventory_shouldFilterZeroQuantityItems() {
        InventoryItem itemWithStock = createInventoryItem("with-stock", "SKU-001", "loc-1", 10, InventoryStatus.ACTIVE);
        InventoryItem itemWithoutStock = createInventoryItem("no-stock", "SKU-001", "loc-2", 0, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(itemWithStock, itemWithoutStock));

        List<InventoryItem> result = allocationService.findAvailableInventory("SKU-001");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId().getValue()).isEqualTo("with-stock");
    }

    @Test
    void findAvailableInventory_shouldSortByQuantityDescending() {
        InventoryItem small = createInventoryItem("small", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        InventoryItem large = createInventoryItem("large", "SKU-001", "loc-2", 100, InventoryStatus.ACTIVE);
        InventoryItem medium = createInventoryItem("medium", "SKU-001", "loc-3", 50, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(small, large, medium));

        List<InventoryItem> result = allocationService.findAvailableInventory("SKU-001");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getId().getValue()).isEqualTo("large");
        assertThat(result.get(1).getId().getValue()).isEqualTo("medium");
        assertThat(result.get(2).getId().getValue()).isEqualTo("small");
    }

    @Test
    void findAvailableInventory_withNoInventory_shouldReturnEmpty() {
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of());

        List<InventoryItem> result = allocationService.findAvailableInventory("SKU-001");

        assertThat(result).isEmpty();
    }

    @Test
    void findAvailableInventory_withAllInactiveOrZero_shouldReturnEmpty() {
        InventoryItem suspended = createInventoryItem("suspended", "SKU-001", "loc-1", 10, InventoryStatus.SUSPENDED);
        InventoryItem zeroStock = createInventoryItem("zero", "SKU-001", "loc-2", 0, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(suspended, zeroStock));

        List<InventoryItem> result = allocationService.findAvailableInventory("SKU-001");

        assertThat(result).isEmpty();
    }

    @Test
    void allocateStock_withPartiallyReservedItems_shouldConsiderAvailableOnly() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 20, InventoryStatus.ACTIVE);
        item1.reserveStock(15, "other-order", userId, id("mov-1")); // 5 available
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 30, InventoryStatus.ACTIVE);
        item2.reserveStock(20, "other-order", userId, id("mov-2")); // 10 available
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        AllocationResult result = allocationService.allocateStock("SKU-001", 12, "order-1", userId);

        assertThat(result.success()).isTrue();
        assertThat(result.allocatedQuantity()).isEqualTo(12);
        assertThat(result.allocations()).hasSize(2);
        assertThat(result.allocations().get(0).inventoryItemId().getValue()).isEqualTo("item-2");
        assertThat(result.allocations().get(0).quantity()).isEqualTo(10);
        assertThat(result.allocations().get(1).inventoryItemId().getValue()).isEqualTo("item-1");
        assertThat(result.allocations().get(1).quantity()).isEqualTo(2);
    }

    @Test
    void allocateStock_withMixedActiveAndInactive_shouldAllocateFromActiveOnly() {
        InventoryItem activeSmall = createInventoryItem("active-small", "SKU-001", "loc-1", 5, InventoryStatus.ACTIVE);
        InventoryItem inactiveLarge = createInventoryItem("inactive-large", "SKU-001", "loc-2", 100, InventoryStatus.DISCONTINUED);
        InventoryItem activeLarge = createInventoryItem("active-large", "SKU-001", "loc-3", 50, InventoryStatus.ACTIVE);
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(activeSmall, inactiveLarge, activeLarge));

        AllocationResult result = allocationService.allocateStock("SKU-001", 20, "order-1", userId);

        assertThat(result.success()).isTrue();
        assertThat(result.allocations()).hasSize(1);
        assertThat(result.allocations().getFirst().inventoryItemId().getValue()).isEqualTo("active-large");
        verify(inventoryRepository, times(1)).save(any());
    }

    @Test
    void deallocateStock_withMoreRequestedThanReserved_shouldReleaseOnlyReserved() {
        InventoryItem item1 = createInventoryItem("item-1", "SKU-001", "loc-1", 100, InventoryStatus.ACTIVE);
        InventoryItem item2 = createInventoryItem("item-2", "SKU-001", "loc-2", 100, InventoryStatus.ACTIVE);
        item1.reserveStock(5, "order-1", userId, id("mov-1"));
        item2.reserveStock(3, "order-1", userId, id("mov-2"));
        when(inventoryRepository.findBySku("SKU-001")).thenReturn(List.of(item1, item2));

        allocationService.deallocateStock("SKU-001", 20, "order-1", userId);

        assertThat(item1.getQuantity().reserved()).isZero();
        assertThat(item2.getQuantity().reserved()).isZero();
        verify(inventoryRepository, times(2)).save(any());
    }

    private InventoryItem createInventoryItem(String itemId, String sku, String locationId, int onHand, InventoryStatus status) {
        return new InventoryItem(
                id(itemId),
                sku,
                id("variant-1"),
                id(locationId),
                InventoryQuantity.withOnHand(onHand),
                null,
                status,
                LocalDateTime.now()
        );
    }

    private Id id(String value) {
        return () -> value;
    }
}
