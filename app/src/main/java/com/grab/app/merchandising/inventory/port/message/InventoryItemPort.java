package com.grab.app.merchandising.inventory.port.message;

import com.grab.framework.id.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Inventory Item and Stock Level operations.
 * Replaces legacy InventoryController endpoints.
 */
public interface InventoryItemPort {

    ItemMessageResult createInventoryItem(CreateInventoryItemMessage message);

    Optional<ItemMessageResult> findItemById(Id itemId);

    Optional<MovementMessageResult> findLatestMovement(Id itemId);

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
    ) {}

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
    ) {}

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
    ) {}
}
