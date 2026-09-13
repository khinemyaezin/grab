# Inventory Allocation Feature

## Overview

The Inventory Allocation feature provides sophisticated stock allocation capabilities across multiple locations, enabling efficient order fulfillment while maintaining inventory accuracy and preventing overselling. It handles complex allocation scenarios with prioritization strategies and comprehensive error handling.

## Business Context

### Problem Statement

E-commerce businesses need to:
- Allocate inventory across multiple warehouses efficiently
- Prevent overselling by validating stock availability
- Reserve stock for orders to ensure fulfillment
- Release reservations when orders are cancelled
- Support location-specific and cross-location allocation
- Track allocation changes with audit trail

### Solution

A domain-driven allocation service that:
- Provides multi-location stock allocation
- Prioritizes inventory by available quantity (highest first)
- Validates allocation feasibility before committing
- Creates audit trail through stock movements
- Supports both reservation and deallocation operations
- Provides real-time availability checks

---

## Core Capabilities

### 1. Multi-Location Stock Allocation

**Description**: Allocate stock across multiple locations intelligently, prioritizing locations with highest available inventory.

**Business Value**:
- Optimizes inventory usage across warehouse network
- Reduces shipping costs by using nearest stock
- Improves order fulfillment speed

**User Stories**:
```
As an order fulfillment system
I want to allocate stock from the best available location
So that orders are fulfilled efficiently

As a warehouse manager
I want to allocate stock from a specific location
So that I can manage warehouse operations

As a buyer
I want to know if stock can be allocated before committing
So that I can make informed business decisions
```
---

### 2. Stock Reservation & Deallocation

**Description**: Reserve allocated stock and release reservations when needed.

**Business Logic**:
```
Available Quantity = OnHand - Reserved

When allocating:
1. Check if available >= requested
2. Create RESERVATION movement
3. Update reserved quantity
4. Save changes

When deallocating:
1. Find inventory items with reserved stock
2. Release up to requested amount
3. Create RESERVATION_RELEASE movement
4. Update reserved quantity
```

**Business Value**:
- Prevents double-allocation of same stock
- Ensures order fulfillment capability
- Supports order cancellation workflows
- Maintains accurate available quantity

**User Stories**:
```
As an order management system
I want to deallocate stock when order is cancelled
So that stock becomes available for other orders

As a fulfillment manager
I want to see reserved vs available stock
So that I can plan operations correctly

As a customer service agent
I want to release allocations on refund
So that inventory is accurate
```

---

### 3. Availability Checking

**Description**: Check if stock can be allocated before committing to orders.

**Use Cases**:
- Order validation before acceptance
- Inventory dashboard display
- Fulfillment planning
- Customer communication

---

### 4. Intelligent Inventory Prioritization

**Description**: Automatically prioritize inventory allocation to optimize fulfillment.

**Business Logic**:

- Allocating stock across multiple locations:
  - Query inventory by SKU from repository
  - Filter: Keep only ACTIVE items
  - Filter: Keep only items with 
    - `availableQuantity > 0 (availableQuantity = max(0, onHand - reserved - damaged))`
  - Sort: By availableQuantity descending (highest first)

**Benefits**:
- Reduces fragmented inventory
- Minimizes future shipping costs
- Balances warehouse loads

**Example Scenario**:
```
Available Inventory for SKU-001:
- Location A: 10 units (oldest stock)
- Location B: 50 units (newest stock)
- Location C: 30 units

Allocation Request: 60 units

Allocation Strategy:
1. Allocate 50 from Location B (highest)
2. Allocate 10 from Location C (second highest)
3. Result: Fulfills from newest stock first
```

---

### 5. Error Handling with Type-Safe Errors

**Description**: Comprehensive error handling for all allocation scenarios.

**Supported Error Types**:

| Error | Fields | Trigger Condition |
|-------|--------|-------------------|
| `QuantityNotPositive` | - | Request quantity ≤ 0 |
| `NoAvailableInventory` | sku | No active inventory for SKU |
| `InsufficientStock` | available, requested | Total stock < requested |
| `InventoryItemNotActive` | sku | Item is SUSPENDED/DISCONTINUED |
| `InventoryNotFoundAtLocation` | sku, locationId | No inventory at location |

---

### Domain Model Impact

```
InventoryItem
├── Quantity
│   ├── onHand: int
│   ├── reserved: int      ← Updated on allocation
│   ├── inTransit: int
│   └── damaged: int
└── Movements
    └── RESERVATION        ← Created on allocation
    └── RESERVATION_RELEASE ← Created on deallocation
```
---

### Events Published
```
AllocationSucceeded
- sku, quantity, locations, orderId, timestamp

AllocationFailed
- sku, quantity, reason, timestamp

StockReserved
- inventoryItemId, quantity, orderId, timestamp

ReservationReleased
- inventoryItemId, quantity, orderId, timestamp
```

### Events Consumed
```
OrderPlaced → Allocate stock
OrderCancelled → Deallocate stock
OrderShipped → Finalize allocation
OrderReturned → Release and reallocate
```

---

## Order / Checkout Integration Contract

Order and checkout callers should treat inventory allocation as a two-phase hold:

| Step | When | API | Effect |
|------|------|-----|--------|
| 1. Check | Before accepting payment / placing order | `GET /api/v1/inventory/allocations/availability?sku=&quantity=` | Soft check; does not hold stock |
| 2. Allocate | On order place / payment authorized | `POST /api/v1/inventory/allocations` | Reserves stock across locations (or one `locationId`), creates `InventoryReservation` rows with optional `expiresAt` |
| 3a. Ship | On fulfillment | `POST /api/v1/inventory/items/{id}/reservations/{reservationId}/ship` | Consumes reserved qty (per reservation line returned by allocate) |
| 3b. Release | On cancel / payment failure | `POST /api/v1/inventory/allocations/deallocate` **or** per-item release | Returns reserved qty to available |
| Expiry | Unshipped hold past `expiresAt` | Background `ReservationExpiryJob` | Marks reservation `EXPIRED` and releases stock |

### Allocate request (order side)

```json
{
  "sku": "SKU-001",
  "quantity": 2,
  "orderId": "ord_123",
  "orderLineId": "line_1",
  "locationId": null,
  "expiresAt": "2026-07-19T15:00:00"
}
```

- Omit `locationId` for multi-location allocation (highest available first).
- Pass `expiresAt` for checkout holds that must auto-release if the order is abandoned.
- Response `allocations[]` lists `reservationId` / `inventoryItemId` / `locationId` / `quantity` for later ship or release.

### Rules

1. **Idempotency**: callers should retry allocate carefully; prefer a stable `orderId` + `orderLineId` and deallocate before re-allocate on amendment.
2. **Ship path**: ship each returned reservation (item-scoped), not a second allocate.
3. **Partial deallocate**: deallocate prefers whole reservation rows for the order+sku; prefer canceling the order line entirely when possible.
4. **Active locations only**: allocation ignores inventory at inactive locations.
