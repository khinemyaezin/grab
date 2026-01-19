package com.inventory.domain.service;

import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.enums.AllocationError;

import java.util.List;

public interface InventoryAllocationService {

    record AllocationResult(
            boolean success,
            String sku,
            int requestedQuantity,
            int allocatedQuantity,
            List<AllocationDetail> allocations,
            AllocationError error,
            String errorMessage
    ) {
        public static AllocationResult success(String sku, int quantity, List<AllocationDetail> allocations) {
            return new AllocationResult(true, sku, quantity, quantity, allocations, null, null);
        }

        public static AllocationResult partialSuccess(String sku, int requested, int allocated, List<AllocationDetail> allocations) {
            return new AllocationResult(false, sku, requested, allocated, allocations,
                    AllocationError.PARTIAL_ALLOCATION,
                    AllocationError.PARTIAL_ALLOCATION.formatMessage(requested, allocated));
        }

        public static AllocationResult failure(String sku, int quantity, AllocationError error, Object... args) {
            return new AllocationResult(false, sku, quantity, 0, List.of(), error, error.formatMessage(args));
        }
    }

    record AllocationDetail(
            Id inventoryItemId,
            Id locationId,
            int quantity
    ) {}

    AllocationResult allocateStock(String sku, int quantity, String orderId);

    AllocationResult allocateStockFromLocation(String sku, Id locationId, int quantity, String orderId);

    void deallocateStock(String sku, int quantity, String orderId);

    boolean canAllocate(String sku, int quantity);

    int getAvailableForAllocation(String sku);

    List<InventoryItem> findAvailableInventory(String sku);
}
