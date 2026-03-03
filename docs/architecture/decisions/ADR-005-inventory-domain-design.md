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
- Encapsulates all stock movements as child entities
- Enforces business rules through aggregate boundaries

```java
InventoryItem (Aggregate Root)
├── id: Id
├── sku: String
├── productVariantId: Id
├── locationId: Id
├── quantity: InventoryQuantity (Value Object)
├── reorderConfig: ReorderConfig (Value Object)
├── status: InventoryStatus
└── movements: List<StockMovement> (Entities)
```

**Rationale**:
- Ensures transactional consistency for inventory changes
- Stock movements always belong to an inventory item
- Aggregate boundary aligns with business invariants
- Prevents orphaned stock movements

### 2. Stock Movement Lifecycle

**Immutable Movement Records**
- Stock movements are created once and never modified
- Each movement records: type, quantity, before/after amounts, timestamp, reference
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

❌ **Storage**
- Movement history grows over time
- May need archival strategy
- More storage than snapshot approach

❌ **Performance**
- Loading full aggregate can be expensive
- Many movements = large object graph
- May need pagination for movements

### Mitigation Strategies

1. **Movement History Management**
   - Implement movement pagination
   - Archive old movements to separate table
   - Provide summary queries

2. **Performance Optimization**
   - Lazy load movements when not needed
   - Use projections for list queries
   - Cache frequently accessed inventory

3. **Developer Onboarding**
   - Comprehensive documentation
   - Example implementations
   - Pair programming sessions

## Alternatives Considered

### Alternative 1: Single Inventory Table with Location Column
**Rejected**: Would require complex queries for transfers and make movement tracking harder.

### Alternative 2: Event Sourcing for Stock Movements
**Deferred**: Added complexity not justified for MVP. Consider for future if advanced time-travel queries needed.

### Alternative 3: CQRS with Separate Read/Write Models
**Deferred**: Current design sufficient for expected load. Revisit if query performance becomes issue.

### Alternative 4: Snapshot + Events Pattern
**Rejected**: Movement history already serves as events. Snapshots add complexity without clear benefit.


## References

- Domain-Driven Design by Eric Evans

