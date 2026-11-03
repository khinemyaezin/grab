package com.grab.app.merchandising.inventory.port.message;

import com.grab.framework.id.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Stock Availability and Real-time Allocation checks.
 * Unifies InventoryAvailabilityQuery, InventoryDiscoveryQuery, and AllocationController.
 */
public interface InventoryAvailabilityPort {

    Optional<DiscoveryStockFactResult> findDiscoveryFacts(Id merchantId, String sku, Id salesChannelId);

    AllocationMessageResult allocateStock(AllocateStockMessage message);

    void deallocateStock(DeallocateStockMessage message);

    List<ReorderSuggestionMessageResult> listReorderSuggestions(Id merchantId, Id locationId, String sku);

    record AllocateStockMessage(
            String sku,
            int quantity,
            String orderId,
            String orderLineId,
            Id locationId,
            LocalDateTime expiresAt,
            Id createdBy,
            String scopeKey,
            String scopeId
    ) {}

    record DeallocateStockMessage(
            String sku,
            int quantity,
            String orderId,
            Id createdBy,
            String scopeKey,
            String scopeId
    ) {}

    record AllocationMessageResult(
            String reservationId,
            String orderId,
            String sku,
            Id locationId,
            int quantity,
            String status,
            LocalDateTime expiresAt
    ) {}

    record DiscoveryStockFactResult(
            int availableQty,
            boolean allowBackorder
    ) {}

    record ReorderSuggestionMessageResult(
            String inventoryItemId,
            String sku,
            String productName,
            String productVariantId,
            String locationId,
            int currentAvailable,
            int reorderPoint,
            int suggestedQuantity,
            String priority
    ) {}
}
