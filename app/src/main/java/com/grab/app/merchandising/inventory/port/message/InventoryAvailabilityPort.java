package com.grab.app.merchandising.inventory.port.message;

import com.grab.framework.id.Id;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Universal Java Port Interface for Stock Availability and Real-time Allocation checks.
 * Unifies InventoryAvailabilityQuery, InventoryDiscoveryQuery, and AllocationController.
 */
public interface InventoryAvailabilityPort {

    boolean checkAvailability(Id variantId, int requestedQuantity);

    Optional<InventoryStockFactResult> findInventoryItemFacts(Id variantId);

    Map<Id, Integer> findStockLevels(List<Id> variantIds);

    AllocationResult allocate(AllocateStockMessage message);

    void deallocate(DeallocateStockMessage message);

    List<ReorderSuggestionResult> listReorderSuggestions(Id merchantId);

    record AllocateStockMessage(
            Id orderId,
            Id variantId,
            int quantity
    ) {}

    record DeallocateStockMessage(
            Id orderId,
            Id variantId,
            int quantity
    ) {}

    record AllocationResult(
            Id allocationId,
            Id orderId,
            Id variantId,
            int quantity,
            String status
    ) {}

    record InventoryStockFactResult(
            Id variantId,
            int onHand,
            int available,
            int reserved,
            boolean inStock
    ) {}

    record ReorderSuggestionResult(
            Id variantId,
            String sku,
            int currentStock,
            int reorderPoint,
            int suggestedQuantity
    ) {}
}

