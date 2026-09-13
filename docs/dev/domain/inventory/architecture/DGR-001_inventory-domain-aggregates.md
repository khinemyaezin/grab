# Inventory Domain Aggregate Diagram

This diagram reflects the current inventory domain model in code.

## Class Diagram

### Location
```mermaid
classDiagram
    direction TD
    namespace LocationAggregate {
        class Location {
            +Id id
            +String code
            +String name
            +LocationType type
            +Address address
            +boolean active
            +List~Zone~ zones
            +create(id, code, name, type, address) Location
            +createWarehouse(id, code, name, address) Location
            +createStore(id, code, name, address) Location
            +addZone(zone) boolean
            +removeZone(zoneId) boolean
            +findZoneById(zoneId) Optional~Zone~
            +findZoneByCode(code) Optional~Zone~
            +getActiveZones() List~Zone~
            +getZonesByType(type) List~Zone~
            +deactivate()
            +activate()
            +getTotalZoneCount() int
            +getActiveZoneCount() int
            +getTotalBinCount() int
        }

        class Zone {
            +Id id
            +String code
            +String name
            +ZoneType type
            +boolean active
            +List~Bin~ bins
            +addBin(bin) boolean
            +removeBin(binId) boolean
            +findBinById(binId) Bin
            +findBinByCode(code) Bin
            +getActiveBins() List~Bin~
            +deactivate()
            +activate()
        }

        class Bin {
            +Id id
            +String code
            +String name
            +Integer maxCapacity
            +boolean active
            +hasCapacityLimit() boolean
            +deactivate()
            +activate()
        }

        class Address {
            <<record>>
            +String line1
            +String line2
            +String city
            +String state
            +String postalCode
            +String country
            +of(line1, city, state, postalCode, country) Address
            +fullAddress() String
        }

        class LocationType {
            <<enumeration>>
            WAREHOUSE
            STORE
        }

        class ZoneType {
            <<enumeration>>
            PICKING
            STORAGE
            STAGING
            RETURNS
            DAMAGED
            RECEIVING
        }
    }
    Location *-- "0..*" Zone : "owns"
    Zone *-- "0..*" Bin : "owns"
    Location --> Address : "value object"
    Location --> LocationType
    Zone --> ZoneType
```
### Inventory
```mermaid
classDiagram
    direction TD

    namespace InventoryItemAggregate {
        class InventoryItem {
            +Id id
            +String sku
            +Id productVariantId
            +Id locationId
            +InventoryQuantity quantity
            +ReorderConfig reorderConfig
            +InventoryStatus status
            +LocalDateTime lastUpdated
            +create(id, sku, productVariantId, locationId, initialQty, reorderConfig) InventoryItem
            +receiveStock(qty, type, ref, notes, userId, movementId) StockMovement
            +reserveStock(qty, orderId, userId, movementId) StockMovement
            +releaseReservation(qty, orderId, userId, movementId) StockMovement
            +shipStock(qty, orderId, userId, movementId) StockMovement
            +adjustStock(newOnHand, reason, notes, userId, movementId) StockMovement
            +markDamaged(qty, notes, userId, movementId) StockMovement
            +writeOff(qty, reason, notes, userId, movementId) StockMovement
            +discontinue()
            +suspend()
            +activate()
            +isLowStock() boolean
            +needsReorder() boolean
            +canFulfill(qty) boolean
            +getAvailableQuantity() int
            +getSuggestedReorderQuantity() int
        }

        class InventoryQuantity {
            <<record>>
            +int onHand
            +int reserved
            +int inTransit
            +int damaged
            +available() int
            +total() int
            +sellable() int
            +addOnHand(qty) InventoryQuantity
            +subtractOnHand(qty) InventoryQuantity
            +reserve(qty) InventoryQuantity
            +releaseReservation(qty) InventoryQuantity
            +markDamaged(qty) InventoryQuantity
            +shipReserved(qty) InventoryQuantity
            +addInTransit(qty) InventoryQuantity
            +receiveInTransit(qty) InventoryQuantity
        }

        class ReorderConfig {
            <<record>>
            +int safetyStock
            +int reorderPoint
            +int reorderQuantity
            +Integer maxStock
            +isLowStock(available) boolean
            +needsReorder(available) boolean
            +wouldExceedMaxStock(currentOnHand, qty) boolean
            +suggestedOrderQuantity(available) int
        }

        class InventoryStatus {
            <<enumeration>>
            ACTIVE
            OUT_OF_STOCK
            SUSPENDED
            DISCONTINUED
        }

        class AdjustmentReason {
            <<enumeration>>
            CYCLE_COUNT
            DAMAGED
            EXPIRED
            LOST
            FOUND
            THEFT
            CORRECTION
        }
    }
    InventoryItem --> InventoryQuantity : "value object"
    InventoryItem --> ReorderConfig : "value object"
    InventoryItem --> InventoryStatus
    InventoryItem --> AdjustmentReason
```

```mermaid
classDiagram
    direction TD
    
    namespace StockMovementWrapper {
        class StockMovement {
            +Id id
            +Id inventoryItemId
            +StockMovementType type
            +int quantity
            +int quantityBefore
            +int quantityAfter
            +int onHandBefore
            +int onHandAfter
            +int reservedBefore
            +int reservedAfter
            +String referenceId
            +LocalDateTime createdAt
            +Id createdBy
            +create(...) StockMovement
            +isInbound() boolean
            +isOutbound() boolean
            +isAdjustment() boolean
            +isReservation() boolean
        }

        class StockMovementType {
            <<enumeration>>
            PURCHASE_ORDER_RECEIPT
            CUSTOMER_RETURN
            TRANSFER_IN
            INITIAL_STOCK
            SALE
            TRANSFER_OUT
            RETURN_TO_VENDOR
            WRITE_OFF
            CYCLE_COUNT_ADJUSTMENT
            DAMAGE_ADJUSTMENT
            SHRINKAGE
            RESERVATION
            RESERVATION_RELEASE
        }
    }

    namespace InventoryReservationWrapper {
        class InventoryReservation {
            +Id id
            +Id inventoryItemId
            +String orderId
            +String orderLineId
            +int quantity
            +String idempotencyKey
            +InventoryReservationStatus status
            +LocalDateTime expiresAt
            +LocalDateTime createdAt
            +LocalDateTime updatedAt
            +create(...) InventoryReservation
            +release()
            +fulfill()
            +expire()
            +cancel()
        }

        class InventoryReservationStatus {
            <<enumeration>>
            ACTIVE
            RELEASED
            FULFILLED
            EXPIRED
            CANCELLED
        }
    }

    StockMovement --> StockMovementType
    InventoryReservation --> InventoryReservationStatus
```    


## Persistence and Event Flow

```mermaid
sequenceDiagram
    participant H as Command Handler
    participant IR as InventoryRepository
    participant LR as LocationRepository
    participant DB as Database
    participant OB as Outbox Table
    participant SM as StockMovementRepository
    participant RR as ReservationRepository
    participant P as OutboxProcessor
    participant D as Dispatcher
    participant C as Consumers

    H->>LR: findById(locationId)
    LR->>DB: SELECT location + zones + bins
    DB-->>LR: Location graph
    LR-->>H: Location (active check)

    H->>IR: findById(inventoryItemId)
    IR->>DB: SELECT inventory_item
    DB-->>IR: InventoryItem
    IR-->>H: InventoryItem

    H->>H: item.reserveStock() → StockMovement

    H->>IR: save(item)
    IR->>DB: UPDATE inventory_item
    IR->>OB: INSERT outbox rows (from item.pullEvents())
    Note over DB,OB: Same transaction

    H->>SM: save(movement)
    SM->>DB: INSERT stock_movement

    H->>RR: save(reservation)
    RR->>DB: INSERT inventory_reservation

    Note over P,C: Async outbox delivery

    P->>OB: Poll NEW/FAILED rows
    P->>OB: Claim batch
    P->>D: Deserialize and dispatch
    D->>C: Deliver as Spring event
    P->>OB: Mark PUBLISHED
```

## Notes

- `InventoryItem` and `Location` are the two aggregate roots in the inventory domain.
- `Zone` and `Bin` are child entities owned by `Location`, loaded and saved as a single aggregate unit.
- `StockMovement` and `InventoryReservation` are independently persisted entities, not part of the `InventoryItem` aggregate.
- All cross-aggregate references use ID only — no object navigation across aggregate boundaries.
- `InventoryItem` stock operations return `StockMovement` entities for the caller to persist independently via `StockMovementRepository`.
- Only `InventoryItem` publishes domain events via the outbox pattern. `Location`, `StockMovement`, and `InventoryReservation` repositories do not publish events. Location events are a planned addition per ADR-008.
- `InventoryItemDiscontinuedEvent` is defined but not yet raised in code.
- Domain services (`InventoryAllocationService`, `ReorderService`) orchestrate cross-aggregate logic without owning state.
- `InventoryItem` uses optimistic locking (`@Version`) to protect concurrent stock modifications.
- `InventoryReservation` supports idempotency via `idempotencyKey` to prevent duplicate reservations.
