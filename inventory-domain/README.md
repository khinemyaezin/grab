# Inventory Domain - Technical Requirements

## Overview

The Inventory Domain module manages stock tracking, warehouse locations, inventory movements, and reorder management following Domain-Driven Design (DDD) principles.

---

## Architecture

```
inventory-domain/
├── aggregate/          # Aggregate Roots
├── entity/             # Domain Entities
├── valueobject/        # Value Objects
├── enums/              # Domain Enumerations
├── event/              # Domain Events
├── exception/          # Domain Exceptions & Error Types
├── service/            # Domain Services
├── specification/      # Business Rule Specifications
├── repository/         # Repository Interfaces
└── factory/            # Factory Interfaces & Implementations
```

---

## Domain Model

### Aggregates

#### InventoryItem (Aggregate Root)
Tracks inventory for a specific SKU at a specific location.

| Field | Type | Description |
|-------|----|-------------|
| id | Id | Unique identifier |
| sku | String | Stock Keeping Unit (required) |
| productVariantId | Id | Reference to product variant |
| locationId | Id | Reference to location (required) |
| quantity | InventoryQuantity | Quantity breakdown |
| reorderConfig | ReorderConfig | Reorder settings |
| status | InventoryStatus | Current status |
| movements | List\<StockMovement\> | Movement history |
| lastUpdated | LocalDateTime | Last modification timestamp |

**Operations:**
- `receiveStock()` - Receive inbound stock
- `reserveStock()` - Reserve stock for an order
- `releaseReservation()` - Release reserved stock
- `shipStock()` - Ship reserved stock
- `adjustStock()` - Adjust stock quantity
- `markDamaged()` - Mark stock as damaged
- `writeOff()` - Write off stock

**Query Methods:**
- `getAvailableQuantity()` - Get available quantity
- `isLowStock()` - Check if below safety stock
- `needsReorder()` - Check if below reorder point
- `isOutOfStock()` - Check if out of stock
- `canFulfill(qty)` - Check if can fulfill order

---

#### Location (Aggregate Root)
Represents a physical location (warehouse, store,).

| Field | Type                  | Description |
|-------|-----------------------|-------------|
| id | Id                    | Unique identifier |
| code | String                | Location code (required) |
| name | String                | Location name (required) |
| type | LocationType          | Type of location |
| address | [Address](#Address)   | Physical address |
| active | boolean               | Active status |
| zones | List\<[Zone](#Zone)\> | Storage zones |

**Factory Methods:**
- `createWarehouse()` - Create warehouse location
- `createStore()` - Create store location
---

### Entities

#### StockMovement
Audit trail for inventory changes.

| Field | Type | Description |
|-------|------|-------------|
| id | Id | Unique identifier |
| type | StockMovementType | Movement type |
| quantity | int | Quantity changed |
| quantityBefore | int | Quantity before change |
| quantityAfter | int | Quantity after change |
| referenceId | String | External reference (order ID, PO, etc.) |
| notes | String | Additional notes |
| createdAt | LocalDateTime | Timestamp |
| createdBy | String | User who made the change |

**Query Methods:**
- `isInbound()` - Check if inbound movement
- `isOutbound()` - Check if outbound movement
- `isAdjustment()` - Check if adjustment
- `isReservation()` - Check if reservation

---

#### Zone
Storage zone within a location. A designated area within a warehouse or store that serves a specific purpose.

| Field  | Type                  | Description |
|--------|-----------------------|-------------|
| id     | Id                    | Unique identifier |
| code   | String                | Zone code (required) |
| name   | String                | Zone name (required) |
| type   | [ZoneType](#ZoneType) | Zone type |
| active | boolean               | Active status |
| bins   | List\<[Bin](#Bin)\>   | Storage bins |

---

#### Bin
Individual storage bin within a zone. A Bin is the smallest addressable storage location - a specific shelf, slot, or container within a zone.

| Field | Type | Description |
|-------|------|-------------|
| id | Id | Unique identifier |
| code | String | Bin code |
| active | boolean | Active status |

---

### Value Objects

#### InventoryQuantity
Immutable representation of inventory quantities.

| Field | Type | Description |
|-------|------|-------------|
| onHand | int | Physical quantity in stock |
| reserved | int | Quantity reserved for orders |
| inTransit | int | Quantity being transferred |
| damaged | int | Damaged quantity |

**Calculated Properties:**
- `available()` = onHand - reserved - damaged
- `total()` = onHand + inTransit
- `sellable()` = onHand - damaged

**Validations:**
- All quantities must be non-negative

---

#### ReorderConfig
Configuration for automatic reorder alerts.

| Field | Type | Description |
|-------|------|-------------|
| safetyStock | int | Minimum stock level |
| reorderPoint | int | Trigger point for reorder |
| reorderQuantity | int | Quantity to order |
| maxStock | Integer | Maximum stock level (optional) |

**Validations:**
- `reorderPoint >= safetyStock`
- All values must be non-negative

**Methods:**
- `isLowStock(qty)` - Check if quantity <= safety stock
- `needsReorder(qty)` - Check if quantity <= reorder point
- `suggestedOrderQuantity(qty)` - Calculate suggested order quantity

---

#### Address
Physical address value object.

| Field | Type | Description |
|-------|------|-------------|
| street | String | Street address |
| city | String | City |
| state | String | State/Province |
| postalCode | String | Postal/ZIP code |
| country | String | Country |

---

### Enumerations

#### InventoryStatus
```
ACTIVE          - Normal operating status
DISCONTINUED    - No longer sold
OUT_OF_STOCK    - Zero available quantity
SUSPENDED       - Temporarily unavailable
```

#### StockMovementType
```
# Inbound (increases stock)
PURCHASE_ORDER_RECEIPT  - Received from supplier
CUSTOMER_RETURN         - Returned by customer
TRANSFER_IN             - Transferred from another location
INITIAL_STOCK           - Initial inventory setup

# Outbound (decreases stock)
SALE                    - Sold to customer
TRANSFER_OUT            - Transferred to another location
RETURN_TO_VENDOR        - Returned to supplier
WRITE_OFF               - Removed from inventory

# Adjustments
CYCLE_COUNT_ADJUSTMENT  - Physical count correction
DAMAGE_ADJUSTMENT       - Damaged goods adjustment
SHRINKAGE               - Unexplained loss

# Reservations
RESERVATION             - Reserved for pending order
RESERVATION_RELEASE     - Released reservation
```

#### LocationType
```
WAREHOUSE       - Distribution center
STORE           - Retail store
```

#### ZoneType
```
PICKING     - Order picking area
STORAGE     - Long-term storage
STAGING     - Order staging/packing area
RETURNS     - Customer returns processing
DAMAGED     - Damaged goods holding
RECEIVING   - Inbound receiving area
```

#### AdjustmentReason
```
CYCLE_COUNT  - Physical inventory count
DAMAGED      - Goods damaged
EXPIRED      - Goods expired
LOST         - Goods lost
FOUND        - Previously lost goods found
THEFT        - Goods stolen
CORRECTION   - Data correction
```

---

## Domain Services

### InventoryAllocationService
Handles stock allocation across multiple locations.

**Methods:**

| Method | Description |
|--------|-------------|
| `allocateStock(sku, qty, orderId, createdBy)` | Allocate stock from any location |
| `allocateStockFromLocation(sku, locationId, qty, orderId, createdBy)` | Allocate from specific location |
| `deallocateStock(sku, qty, orderId, initiatedBy)` | Release allocated stock |
| `canAllocate(sku, qty)` | Check if allocation is possible |
| `getAvailableForAllocation(sku)` | Get total available quantity |
| `findAvailableInventory(sku)` | Get inventory items sorted by priority |

**Result Types:**
- `AllocationResult` - Success/failure with typed error details
- `AllocationDetail` - Location-specific allocation info

**Error Handling:**

The service uses a sealed interface pattern for type-safe, i18n-ready error handling:

```java
AllocationResult result = allocationService.allocateStock(sku, qty, orderId, userId);

if (!result.success()) {
    // Pattern matching for specific error handling
    switch (result.error()) {
        case AllocationError.InsufficientStock(var available, var requested) ->
            log.warn("Not enough stock: {} vs {}", available, requested);
        case AllocationError.NoAvailableInventory(var sku) ->
            log.warn("No inventory for SKU: {}", sku);
        // ... other cases
    }
}
```

---

### InventoryTransferService
Handles inventory transfers between locations.

**Methods:**

| Method | Description |
|--------|-------------|
| `transfer(sku, source, dest, qty, notes)` | Immediate transfer |
| `initiateTransfer(sku, source, dest, qty, notes)` | Start async transfer (in-transit) |
| `completeTransfer(transferId, actualQty)` | Complete pending transfer |
| `cancelTransfer(transferId, reason)` | Cancel pending transfer |
| `canTransfer(sku, source, qty)` | Check if transfer is possible |

---

### ReorderService
Handles reorder point monitoring and suggestions.

---

## Domain Events

| Event | Trigger | Payload |
|-------|---------|---------|
| `StockReceivedEvent` | Stock received | id, sku, qty, locationId, timestamp |
| `StockReservedEvent` | Stock reserved | id, sku, qty, orderId, timestamp |
| `StockShippedEvent` | Stock shipped | id, sku, qty, orderId, timestamp |
| `StockAdjustedEvent` | Stock adjusted | id, sku, before, after, reason, timestamp |
| `LowStockAlertEvent` | Stock below threshold | id, sku, currentQty, reorderPoint, timestamp |
| `InventoryItemDiscontinuedEvent` | Item discontinued | id, sku, timestamp |

---

## Exception Handling

The inventory domain uses a structured approach to error handling that supports type safety, localization, and pattern matching.

### AllocationError (Sealed Interface)

Type-safe error representations for allocation operations. Implements `MessageSource` for i18n support.

| Error Type | Fields | Description |
|------------|--------|-------------|
| `QuantityNotPositive` | - | Requested quantity is zero or negative |
| `NoAvailableInventory` | sku | No active inventory found for SKU |
| `InsufficientStock` | available, requested | Not enough stock to fulfill request |
| `InventoryItemNotActive` | sku | Inventory item is suspended/discontinued |
| `InventoryNotFoundAtLocation` | sku, locationId | No inventory at specified location |

**Usage:**
```java
// Creating errors with type safety
new AllocationError.InsufficientStock(available, requested)

// Accessing error data
if (error instanceof AllocationError.InsufficientStock e) {
    int available = e.available();
    int requested = e.requested();
}

// i18n support
String code = error.code();           // "error.allocation.insufficient_stock"
Map<String, Object> args = error.args(); // {available: 5, requested: 10}
```

### Domain Exceptions

| Exception | Description |
|-----------|-------------|
| `InsufficientQuantityException` | Thrown when quantity operations fail due to insufficient stock |

---

## Localization Support

Error messages support internationalization through message codes and named arguments.

**Message Properties Example:**
```properties
# messages_en.properties
error.allocation.quantity_not_positive=Quantity must be positive
error.allocation.no_available_inventory=No available inventory found for SKU: {sku}
error.allocation.insufficient_stock=Insufficient stock. Available: {available}, Requested: {requested}
error.allocation.inventory_item_not_active=Inventory item {sku} is not active
error.allocation.inventory_not_found_at_location=No inventory found for SKU {sku} at location {locationId}
```

**Benefits of Named Arguments:**
- Language-independent argument order
- Self-documenting templates
- Easy refactoring

---
## Repositories

### InventoryRepository
```java
// Core CRUD operations
Optional<InventoryItem> findById(Id id);
Optional<InventoryItem> findBySku(String sku);
List<InventoryItem> findByLocationId(Id locationId);
InventoryItem save(InventoryItem item);
void delete(Id id);

// Query operations
List<InventoryItem> findLowStock();
List<InventoryItem> findNeedsReorder();
```

### LocationRepository
```java
Optional<Location> findById(Id id);
Optional<Location> findByCode(String code);
List<Location> findByType(LocationType type);
Location save(Location location);
```

### StockMovementRepository
```java
List<StockMovement> findByInventoryItemId(Id inventoryItemId);
List<StockMovement> findByReferenceId(String referenceId);
List<StockMovement> findByDateRange(LocalDateTime start, LocalDateTime end);
```

---

## Business Rules

### Stock Operations
1. Quantities must always be positive for operations
2. Cannot reserve more than available quantity
3. Cannot ship more than reserved quantity
4. Cannot write off more than available quantity
5. Cannot release more than reserved quantity

### Status Transitions
1. ACTIVE -> OUT_OF_STOCK: When available quantity reaches zero
2. OUT_OF_STOCK -> ACTIVE: When available quantity becomes positive
3. Any -> DISCONTINUED: Manual discontinuation
4. Any -> SUSPENDED: Manual suspension

### Reorder Configuration
1. Reorder point must be >= safety stock
2. Low stock alert triggers when quantity <= safety stock
3. Reorder needed when quantity <= reorder point

---

## Technical Stack

- **Java Version:** 21
- **Build Tool:** Maven
- **Libraries:**
  - Lombok - Boilerplate reduction
  - MapStruct - Object mapping
  - JUnit 5 - Testing
  - Mockito - Mocking
  - AssertJ - Fluent assertions

---

## Dependencies

```xml
<dependency>
    <groupId>com.coolstuff.ecommerce.grab</groupId>
    <artifactId>framework</artifactId>
    <version>0.0.1</version>
</dependency>
```

The module depends on the `framework` module which provides:
- `AggregateRoot<T>` - Base class for aggregates
- `Entity<T>` - Base class for entities
- `Id` - Identifier value object

---