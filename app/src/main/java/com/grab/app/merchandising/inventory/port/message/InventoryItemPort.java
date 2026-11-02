package com.grab.app.merchandising.inventory.port.message;

import com.grab.framework.id.Id;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Universal Java Port Interface for Inventory Item and Stock Level operations.
 * Replaces legacy InventoryController endpoints.
 */
public interface InventoryItemPort {

    InventoryItemResult createInventoryItem(CreateInventoryItemMessage message);

    void receiveStock(Id itemId, ReceiveStockMessage message);

    ReservationResult reserveStock(Id itemId, ReserveStockMessage message);

    void releaseReservation(Id itemId, Id reservationId);

    void shipReservation(Id itemId, Id reservationId);

    void adjustStock(Id itemId, AdjustStockMessage message);

    void markDamaged(Id itemId, MarkDamagedMessage message);

    void writeOffStock(Id itemId, WriteOffStockMessage message);

    void returnToVendor(Id itemId, ReturnToVendorMessage message);

    void transferStock(Id itemId, TransferStockMessage message);

    void updateReorderConfig(Id itemId, UpdateReorderConfigMessage message);

    void suspendItem(Id itemId);

    void activateItem(Id itemId);

    void discontinueItem(Id itemId);

    Optional<InventoryItemResult> findItemById(Id itemId);

    InventorySummaryResult getInventorySummary(Id merchantId);

    List<InventoryItemResult> searchInventoryItems(InventorySearchCriteria criteria);

    List<MovementResult> listMovements(Id itemId, int page, int size);

    List<ReservationResult> listReservations(Id itemId, int page, int size);

    record CreateInventoryItemMessage(
            Id merchantId,
            Id variantId,
            String sku,
            Id locationId,
            Id zoneId,
            Id binId,
            int initialQuantity,
            int safetyStock
    ) {}

    record ReceiveStockMessage(
            int quantity,
            String batchNumber,
            String reference
    ) {}

    record ReserveStockMessage(
            int quantity,
            Id orderId,
            Instant expiresAt
    ) {}

    record AdjustStockMessage(
            int adjustedQuantity,
            String reason
    ) {}

    record MarkDamagedMessage(
            int quantity,
            String notes
    ) {}

    record WriteOffStockMessage(
            int quantity,
            String reason
    ) {}

    record ReturnToVendorMessage(
            int quantity,
            String vendorId,
            String reason
    ) {}

    record TransferStockMessage(
            int quantity,
            Id sourceLocationId,
            Id targetLocationId
    ) {}

    record UpdateReorderConfigMessage(
            int reorderPoint,
            int reorderQuantity
    ) {}

    record InventorySearchCriteria(
            Id merchantId,
            Id locationId,
            String sku,
            String status,
            int page,
            int size
    ) {}

    record InventoryItemResult(
            Id id,
            Id merchantId,
            Id variantId,
            String sku,
            Id locationId,
            int onHandQuantity,
            int reservedQuantity,
            int availableQuantity,
            int safetyStock,
            String status,
            Instant updatedAt
    ) {}

    record ReservationResult(
            Id reservationId,
            Id itemId,
            int quantity,
            Id orderId,
            String status,
            Instant expiresAt,
            Instant createdAt
    ) {}

    record MovementResult(
            Id movementId,
            Id itemId,
            String movementType,
            int quantityChange,
            int resultantQuantity,
            String reference,
            Instant occurredAt
    ) {}

    record InventorySummaryResult(
            Id merchantId,
            int totalSkus,
            int totalOnHand,
            int totalReserved,
            int totalAvailable,
            int lowStockCount
    ) {}
}

