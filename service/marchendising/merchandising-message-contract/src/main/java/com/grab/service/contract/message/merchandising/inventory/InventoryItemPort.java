package com.grab.service.contract.message.merchandising.inventory;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.grab.framework.id.Id;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Universal Java Port Interface for Inventory Item and Stock Level operations.
 * Replaces legacy InventoryController endpoints.
 */
public interface InventoryItemPort extends Remote {

    ItemMessageResult createInventoryItem(CreateInventoryItemMessage message) throws RemoteException;

    ItemMessageResult findItemById(Id itemId) throws RemoteException;

    MovementMessageResult findLatestMovement(Id itemId) throws RemoteException;

    record CreateInventoryItemMessage(
            String sku,
            String variantId,
            Id merchantId,
            Id locationId,
            int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock,
            Id createdBy,
            String scopeKey,
            String scopeId
    ) implements Serializable {}

    record ItemMessageResult(
            Id id,
            String sku,
            String productName,
            Id locationId,
            String locationCode,
            String locationName,
            int onHand,
            int reserved,
            int inTransit,
            int damaged,
            int available,
            String status,
            int safetyStock,
            int reorderPoint,
            int reorderQuantity,
            Integer maxStock
    ) implements Serializable {}

    record MovementMessageResult(
            Id id,
            Id inventoryItemId,
            String type,
            int quantity,
            int quantityBefore,
            int quantityAfter,
            int onHandBefore,
            int onHandAfter,
            int reservedBefore,
            int reservedAfter,
            String referenceId,
            LocalDateTime createdAt
    ) implements Serializable {}
}
