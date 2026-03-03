# Feature: Inventory Management

## Overview
**Goal:** Track product stock levels across multiple locations with a complete, immutable audit trail of all movements.
**User Story:** As a warehouse manager, I want to manage inventory per product-location with full movement history, so that I can maintain accurate stock levels, prevent overselling, and satisfy compliance audits.

## Domain Context (DDD)
- **Bounded Context:** Inventory
- **Primary Aggregate:** `InventoryItem` (unique per SKU + Location)
- **Key Entities:** `StockMovement` (independently persisted, linked by `inventoryItemId`), `Location`, `Bin`, `Zone`
- **Value Objects:** `InventoryQuantity` (onHand, reserved, inTransit, damaged), `ReorderConfig`, `Address`
- **New/Modified Rules:**
  - SKU + Location combination must be unique
  - Available quantity (`onHand - reserved - damaged`) cannot go negative
  - Stock movements are immutable — corrections require compensating movements
  - Quantity must be positive for all operations
  - Status auto-transitions to `OUT_OF_STOCK` when available = 0, and back to `ACTIVE` when replenished
  - Only valid inbound types (`PURCHASE_ORDER_RECEIPT`, `CUSTOMER_RETURN`, `TRANSFER_IN`, `INITIAL_STOCK`) accepted by `receiveStock()`
- **Domain Events:**
  - `StockReceivedEvent`: Triggered when stock is received (purchase, return, transfer-in, initial)
  - `StockReservedEvent`: Triggered when stock is reserved for an order
  - `StockShippedEvent`: Triggered when reserved stock is shipped (sale)
  - `StockAdjustedEvent`: Triggered on cycle-count adjustments or damage marking
  - `LowStockAlertEvent`: Triggered when available quantity drops below reorder point
  - `InventoryItemDiscontinuedEvent`: Triggered when an item is discontinued

## Implementation Details

### Entry Points
| Operation | Endpoint | Handler |
|---|---|---|
| Create inventory | `POST /api/inventories` | `InventoryItemFactory.create()` |
| Receive stock | `POST /api/inventories/{id}/movements` (inbound types) | `InventoryItem.receiveStock()` |
| Reserve stock | `POST /api/inventories/{id}/reserve` | `InventoryItem.reserveStock()` |
| Release reservation | `POST /api/inventories/{id}/release` | `InventoryItem.releaseReservation()` |
| Ship stock | `POST /api/inventories/{id}/ship` | `InventoryItem.shipStock()` |
| Adjust stock | `POST /api/inventories/{id}/adjust` | `InventoryItem.adjustStock()` |
| Write off | `POST /api/inventories/{id}/write-off` | `InventoryItem.writeOff()` |
| Mark damaged | `POST /api/inventories/{id}/damage` | `InventoryItem.markDamaged()` |
| Allocate across locations | — | `InventoryAllocationService.allocateStock()` |
| Query movements | `GET /api/inventories/{id}/movements` | `StockMovementRepository` |

### Core Logic
- **Aggregate:** `InventoryItem` encapsulates all mutations; every mutation returns an immutable `StockMovement` (recording type, quantity, before/after, reference, timestamp, and user) that is saved independently via `StockMovementRepository`.
- **Quantity calculation:** `StockMovement.calculateQuantityAfter()` derives `quantityAfter` from movement type direction.
- **Allocation:** `DefaultInventoryAllocationService` finds available inventory by SKU across locations and reserves stock.
- **Reorder:** `DefaultReorderService` evaluates `ReorderConfig` thresholds to recommend replenishment.

### Persistence
- `InventoryRepository` — CRUD for `InventoryItem` (with UNIQUE constraint on `sku + location_id`)
- `StockMovementRepository` — save and query (by inventory item, date range, type, reference)
- `LocationRepository` — location lookup
- Movements are independently persisted and not cascade-deleted with `InventoryItem`

## Acceptance Criteria

### Inventory Lifecycle
- [ ] Inventory item can be created per SKU + Location with optional initial stock
- [ ] Duplicate SKU + Location is rejected
- [ ] Status transitions: `ACTIVE` ↔ `OUT_OF_STOCK` (automatic), `SUSPENDED`, `DISCONTINUED` (manual)

### Stock Movements & Audit Trail
- [ ] Every `receiveStock`, `reserveStock`, `releaseReservation`, `shipStock`, `adjustStock`, `markDamaged`, and `writeOff` call creates an immutable `StockMovement`
- [ ] Each movement records: `type`, `quantity`, `quantityBefore`, `quantityAfter`, `referenceId`, `createdAt`, `createdBy`
- [ ] Movements are never updated or deleted through business operations
- [ ] Movement history is queryable by inventory item, type, date range, and reference

### Quantity Rules
- [ ] `available()` returns `onHand - reserved - damaged` (minimum 0)
- [ ] `InsufficientQuantityException` is thrown when reserving/shipping/writing-off more than available/reserved
- [ ] Negative quantity input is rejected with `IllegalArgumentException`

### Events
- [ ] `StockReceivedEvent` is published on inbound movements
- [ ] `StockReservedEvent` is published when stock is reserved
- [ ] `StockShippedEvent` is published when reserved stock is shipped
- [ ] `StockAdjustedEvent` is published on cycle-count adjustments and damage marking
- [ ] `LowStockAlertEvent` is published when available drops below `reorderPoint`

### Allocation
- [ ] `InventoryAllocationService.allocateStock()` reserves across locations and returns `AllocationResult`
- [ ] Allocation fails with `AllocationError` when insufficient stock exists globally
