# ADR-005: Inventory Domain Design with Multi-Location Support

## Status
Proposed

## Context

The e-commerce platform requires a robust inventory management system that can:
- Track product stock levels across multiple physical locations (warehouses, stores, distribution centers)
- Maintain a complete audit trail of all stock movements
- Support concurrent inventory operations without data inconsistencies
- Enable real-time stock availability queries
- Provide historical inventory analysis capabilities

The system must handle high-volume operations while maintaining data integrity and accuracy.

## Decision

We will implement a Domain-Driven Design (DDD) approach for the inventory domain with the following key architectural decisions:

### 1. Aggregate Design

**InventoryItem as Aggregate Root**
- Each `InventoryItem` represents stock for a specific product at a specific location
- Unique constraint on (SKU, Location) combination
- Enforces business rules through aggregate boundaries
- Mutation methods return `StockMovement` records that are persisted independently

```java
InventoryItem (Aggregate Root)
├── id: Id
├── sku: String
├── productVariantId: Id
├── locationId: Id
├── quantity: InventoryQuantity (Value Object)
├── reorderConfig: ReorderConfig (Value Object)
└── status: InventoryStatus

StockMovement (Entity)
├── id: Id
├── inventoryItemId: Id
├── type: StockMovementType
├── quantity: int
├── quantityBefore: int
├── quantityAfter: int
├── referenceId: String
├── createdAt: LocalDateTime
└── createdBy: Id
```

**Rationale**:
- Ensures transactional consistency for inventory changes
- Aggregate boundary aligns with business invariants (InventoryQuantity holds all state)
- StockMovement is decoupled from the aggregate to avoid unbounded growth
- Movements are append-only audit records — no business logic reads them from the aggregate
- Each movement references its parent via `inventoryItemId`

### 2. Stock Movement Lifecycle

**Immutable Movement Records (Independently Persisted)**
- Stock movements are created once and never modified
- Each movement records: type, quantity, before/after amounts, timestamp, reference, and `inventoryItemId`
- Mutations on `InventoryItem` return a `StockMovement` which is saved via `StockMovementRepository`
- Corrections require compensating movements
- Movement types categorized by direction:
  - **Inbound**: PURCHASE_ORDER_RECEIPT, CUSTOMER_RETURN, TRANSFER_IN, INITIAL_STOCK
  - **Outbound**: SALE, TRANSFER_OUT, RETURN_TO_VENDOR, WRITE_OFF
  - **Adjustments**: CYCLE_COUNT_ADJUSTMENT, DAMAGE_ADJUSTMENT, SHRINKAGE
  - **Reservations**: RESERVATION, RESERVATION_RELEASE

**Rationale**:
- Provides immutable audit trail
- Supports compliance requirements
- Enables historical analysis and reconciliation
- Simplifies debugging and troubleshooting

### 3. Quantity Management

**InventoryQuantity Value Object**
```java
InventoryQuantity {
    - onHand: int (physical stock)
    - reserved: int (allocated to orders)
    - inTransit: int (incoming stock)
    - damaged: int (unusable stock)
    
    Methods:
    - available(): int (onHand - reserved)
    - total(): int (onHand + inTransit)
}
```

**Rationale**:
- Separates physical stock from available stock
- Supports order reservation/allocation
- Prevents overselling
- Clear visibility of stock status

### 4. Multi-Location Support

**Location as First-Class Concept**
- Each inventory item tied to specific location
- Location ID stored in both inventory and movements
- Transfers represented as paired OUT/IN movements
- Independent stock levels per location

**Rationale**:
- Supports multi-warehouse operations
- Enables location-based fulfillment
- Clear tracking of inter-location transfers
- Scalable to many locations

## Consequences

### Positive

✅ **Data Integrity**
- Aggregate boundaries enforce business rules
- Unique constraint prevents duplicate records
- Cascade operations maintain consistency

✅ **Auditability**
- Complete movement history
- Immutable records
- Timestamp tracking
- Reference numbers for external systems

✅ **Scalability**
- Independent inventory per location
- Indexed queries perform well
- Supports high transaction volume

✅ **Testability**
- Domain logic isolated from infrastructure
- Clear mapper responsibilities
- Easy to mock dependencies

✅ **Flexibility**
- Easy to add new movement types
- Extensible quantity tracking
- Location-agnostic design

### Negative

❌ **Complexity**
- More code than simple CRUD
- Developers need DDD knowledge
- Assembler pattern adds indirection
- Callers must remember to save returned `StockMovement` via `StockMovementRepository`

❌ **Storage**
- Movement history grows over time
- May need archival strategy
- More storage than snapshot approach

### Mitigation Strategies

1. **Movement History Management**
   - Movements are decoupled from the aggregate — no performance impact on inventory reads
   - `StockMovementRepository` supports paginated and date-range queries
   - Archive old movements to separate table when needed

2. **Performance Optimization**
   - Inventory reads never load movements (decoupled by design)
   - Use projections for list queries
   - Cache frequently accessed inventory

3. **Developer Onboarding**
   - Comprehensive documentation
   - Example implementations
   - Pair programming sessions

## Alternatives Considered

### Alternative 1: CQRS with Separate Read/Write Models
**Deferred**: Current design sufficient for expected load. Revisit if query performance becomes issue.


## References

- Domain-Driven Design by Eric Evans

