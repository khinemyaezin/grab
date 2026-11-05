package com.grab.app.platform.seller.port.outbound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Seller Outbound Port for Inventory, Warehouse Facilities, and Allocations.
 */
public interface SellerInventoryOutboundPort {

    InventoryItemOutboundResult createItem(CreateItemRecord record);

    Optional<InventoryItemOutboundResult> findItemById(String itemId);

    Optional<StockMovementOutboundResult> findLatestMovement(String itemId);

    AllocationOutboundResult allocateStock(AllocateStockRecord record);

    void deallocateStock(DeallocateStockRecord record);

    List<ReorderSuggestionOutboundResult> listReorderSuggestions(String merchantId, String locationId, String sku);

    FacilityLocationOutboundResult createLocation(CreateLocationRecord record);

    Optional<FacilityLocationOutboundResult> findLocationById(String locationId);

    void activateLocation(String locationId);

    void deactivateLocation(String locationId);

    void deleteLocation(String locationId);

    FacilityZoneOutboundResult createZone(CreateZoneRecord record);

    Optional<FacilityZoneOutboundResult> findZoneById(String zoneId);

    void activateZone(String zoneId);

    void deactivateZone(String zoneId);

    void deleteZone(String zoneId);

    FacilityBinOutboundResult createBin(CreateBinRecord record);

    Optional<FacilityBinOutboundResult> findBinById(String binId);

    void activateBin(String binId);

    void deactivateBin(String binId);

    void deleteBin(String binId);

    record CreateItemRecord(
            String merchantId,
            String actorId,
            String sku,
            String locationId,
            int initialQuantity,
            Integer safetyStock,
            Integer reorderPoint,
            Integer reorderQuantity,
            Integer maxStock
    ) {}

    record AllocateStockRecord(
            String actorId,
            String sku,
            String locationId,
            int quantity,
            String orderId
    ) {}

    record DeallocateStockRecord(
            String actorId,
            String sku,
            int quantity,
            String orderId
    ) {}

    record CreateLocationRecord(
            String merchantId,
            String actorId,
            String code,
            String name,
            String type,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country
    ) {}

    record CreateZoneRecord(
            String locationId,
            String actorId,
            String code,
            String name,
            String type
    ) {}

    record CreateBinRecord(
            String zoneId,
            String actorId,
            String code,
            String name,
            Integer maxCapacity
    ) {}

    record InventoryItemOutboundResult(
            String id,
            String sku,
            String productName,
            String locationId,
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

    record StockMovementOutboundResult(
            String id,
            String inventoryItemId,
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

    record AllocationOutboundResult(
            String reservationId,
            String orderId,
            String sku,
            String locationId,
            int quantity,
            String status,
            LocalDateTime expiresAt
    ) {}

    record ReorderSuggestionOutboundResult(
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

    record FacilityLocationOutboundResult(
            String id,
            String merchantId,
            String code,
            String name,
            String type,
            boolean active
    ) {}

    record FacilityZoneOutboundResult(
            String id,
            String locationId,
            String code,
            String name,
            String type,
            boolean active
    ) {}

    record FacilityBinOutboundResult(
            String id,
            String zoneId,
            String code,
            String name,
            Integer maxCapacity,
            boolean active
    ) {}
}
