package com.grab.service.contract.message.merchandising.inventory;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Universal Java Port Interface for Stock Availability and Real-time Allocation checks.
 * Unifies InventoryAvailabilityQuery, InventoryDiscoveryQuery, and AllocationController.
 */
public interface InventoryAvailabilityPort extends Remote {

    DiscoveryStockFactResult findDiscoveryFacts(Id merchantId, String sku, Id salesChannelId) throws RemoteException;

    AllocationMessageResult allocateStock(AllocateStockMessage message) throws RemoteException;

    void deallocateStock(DeallocateStockMessage message) throws RemoteException;

    List<ReorderSuggestionMessageResult> listReorderSuggestions(Id merchantId, Id locationId, String sku) throws RemoteException;

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
    ) implements Serializable {}

    record DeallocateStockMessage(
            String sku,
            int quantity,
            String orderId,
            Id createdBy,
            String scopeKey,
            String scopeId
    ) implements Serializable {}

    record AllocationMessageResult(
            String reservationId,
            String orderId,
            String sku,
            Id locationId,
            int quantity,
            String status,
            LocalDateTime expiresAt
    ) implements Serializable {}

    record DiscoveryStockFactResult(
            int availableQty,
            boolean allowBackorder
    ) implements Serializable {}

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
    ) implements Serializable {}
}
