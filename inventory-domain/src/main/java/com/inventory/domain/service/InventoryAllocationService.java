package com.inventory.domain.service;

import com.grab.framework.exception.MessageSource;
import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.exception.InventoryDomainError;

import java.util.List;

/** Service for allocating inventory to orders.
 * <p>This service handles the logic for reserving stock for orders, 
 * ensuring that inventory is allocated in a way that prevents overselling and maintains accurate stock levels.
 * </p>
 * <p>Feature documentation:
 * See docs/features/inventory-allocation.md
 **/
public interface InventoryAllocationService {

    record AllocationResult(
            boolean success,
            String sku,
            int requestedQuantity,
            int allocatedQuantity,
            List<AllocationDetail> allocations,
            MessageSource error
    ) {
        public static AllocationResult success(String sku, int quantity, List<AllocationDetail> allocations) {
            return new AllocationResult(true, sku, quantity, quantity, allocations, null);
        }

        public static AllocationResult failure(String sku, int quantity, InventoryDomainError error) {
            return new AllocationResult(false, sku, quantity, 0, List.of(), error);
        }
    }

    record AllocationDetail(
            Id inventoryItemId,
            Id locationId,
            int quantity
    ) {}

    AllocationResult allocateStock(String sku, int quantity, String orderId, Id createdBy);

    AllocationResult allocateStockFromLocation(String sku, Id locationId, int quantity, String orderId, Id createdBy);

    void deallocateStock(String sku, int quantity, String orderId, Id initiatedBy);

    boolean canAllocate(String sku, int quantity);

    int getAvailableForAllocation(String sku);

    List<InventoryItem> findAvailableInventory(String sku);
}
