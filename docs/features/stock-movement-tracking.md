# Stock Movement Tracking Feature

## Overview

Stock Movement Tracking provides a complete, immutable audit trail of all inventory changes. Every stock adjustment, receipt, sale, transfer, or correction is recorded as a movement with full context, enabling comprehensive inventory analysis and compliance reporting.

## Business Need

### Problems Solved
- **Audit Trail**: Complete visibility into all inventory changes for compliance and investigation
- **Accountability**: Track who made changes and when
- **Root Cause Analysis**: Understand why discrepancies occurred
- **Historical Analysis**: Analyze inventory trends and patterns
- **Reconciliation**: Match physical counts with system records
- **Compliance**: Meet regulatory requirements (SOX, FDA, tax authorities)

### Business Impact
- Reduce inventory discrepancies by 80%
- Improve audit efficiency by 60%
- Enable same-day discrepancy investigation
- Support regulatory compliance requirements
- Provide data for inventory optimization

---

## Movement Types

### Inbound Movements (Increase Stock)

#### PURCHASE_ORDER_RECEIPT
**Description**: Stock received from a supplier/vendor

**Use Cases**:
- Regular supplier deliveries
- Purchase order fulfillment
- Drop shipments
- International imports

**Required Fields**:
- quantity: Amount received
- referenceId: Purchase order number
- createdBy: Receiving clerk

**Optional Fields**:
- notes: Delivery condition, partial receipt notes

**Example**:
```json
{
  "type": "PURCHASE_ORDER_RECEIPT",
  "quantity": 500,
  "referenceId": "PO-2026-001234",
  "createdBy": "user-receiving-clerk-01",
  "notes": "Partial receipt - 200 units backordered"
}
```

**Business Rules**:
- Must match open purchase order
- Triggers quality inspection workflow
- Updates supplier lead time metrics
- May trigger putaway task in WMS

---

#### CUSTOMER_RETURN
**Description**: Product returned by a customer

**Use Cases**:
- Online order returns
- Store returns
- Warranty returns
- Exchange processing

**Required Fields**:
- quantity: Amount returned
- referenceId: Original order number
- createdBy: Returns processor

**Optional Fields**:
- notes: Return reason, condition assessment

**Example**:
```json
{
  "type": "CUSTOMER_RETURN",
  "quantity": 1,
  "referenceId": "ORDER-2026-567890",
  "createdBy": "user-returns-01",
  "notes": "Wrong size ordered - item in perfect condition"
}
```

**Business Rules**:
- Verify return authorization
- Inspect for damage before restocking
- May route to damaged/quarantine location
- Triggers refund/exchange processing

---

#### TRANSFER_IN
**Description**: Stock transferred from another location

**Use Cases**:
- Warehouse-to-warehouse transfers
- DC-to-store replenishment
- Cross-dock operations
- Inventory rebalancing

**Required Fields**:
- quantity: Amount received
- referenceId: Transfer order number
- sourceLocationId: Origin location

**Example**:
```json
{
  "type": "TRANSFER_IN",
  "quantity": 100,
  "referenceId": "XFER-2026-001",
  "sourceLocationId": "WH-CENTRAL",
  "destinationLocationId": "WH-WEST",
  "createdBy": "user-transfer-receiver-01"
}
```

**Business Rules**:
- Must have matching TRANSFER_OUT at source
- Updates in-transit inventory
- Validates shipment quantity
- May trigger quality check

---

#### INITIAL_STOCK
**Description**: Initial inventory setup or opening balance

**Use Cases**:
- System go-live inventory load
- New location setup
- New product introduction
- System migration

**Required Fields**:
- quantity: Initial stock count
- referenceId: Physical count sheet number

**Example**:
```json
{
  "type": "INITIAL_STOCK",
  "quantity": 1000,
  "referenceId": "PHYSICAL-COUNT-2026-Q1",
  "createdBy": "user-inventory-manager-01",
  "notes": "Opening balance for new warehouse"
}
```

**Business Rules**:
- Typically used only once per product-location
- Requires management approval
- Should match physical count documentation
- Sets baseline for future accuracy metrics

---

### Outbound Movements (Decrease Stock)

#### SALE
**Description**: Product sold to a customer

**Use Cases**:
- E-commerce order fulfillment
- POS retail sales
- B2B sales
- Marketplace orders

**Required Fields**:
- quantity: Amount sold
- referenceId: Order/invoice number
- createdBy: Order processor/picker

**Example**:
```json
{
  "type": "SALE",
  "quantity": 2,
  "referenceId": "ORDER-2026-789012",
  "createdBy": "user-picker-05",
  "notes": "Picked from location A-12-3"
}
```

**Business Rules**:
- Must have valid order reference
- Releases any reservations
- Triggers shipping workflow
- Updates COGS calculations

---

#### TRANSFER_OUT
**Description**: Stock transferred to another location

**Use Cases**:
- Store replenishment from DC
- Inter-warehouse transfers
- Return to central warehouse
- Emergency stock transfers

**Required Fields**:
- quantity: Amount transferred
- referenceId: Transfer order number
- destinationLocationId: Target location

**Example**:
```json
{
  "type": "TRANSFER_OUT",
  "quantity": 50,
  "referenceId": "XFER-2026-002",
  "sourceLocationId": "WH-EAST",
  "destinationLocationId": "STORE-NYC-01",
  "createdBy": "user-transfer-shipper-01"
}
```

**Business Rules**:
- Creates in-transit inventory
- Awaits TRANSFER_IN confirmation
- Tracks shipment status
- May affect both locations' available stock

---

#### RETURN_TO_VENDOR
**Description**: Stock returned to supplier

**Use Cases**:
- Defective merchandise returns
- Overstock returns
- End of season returns
- Supplier recall compliance

**Required Fields**:
- quantity: Amount returned
- referenceId: RMA/RGA number
- createdBy: Returns coordinator

**Example**:
```json
{
  "type": "RETURN_TO_VENDOR",
  "quantity": 25,
  "referenceId": "RMA-2026-VENDOR123",
  "createdBy": "user-vendor-returns-01",
  "notes": "Manufacturing defect - batch 2026Q1"
}
```

**Business Rules**:
- Requires vendor authorization
- Triggers credit memo processing
- Updates vendor quality metrics
- May require special packaging/shipping

---

#### WRITE_OFF
**Description**: Stock removed from inventory without recovery

**Use Cases**:
- Damaged beyond repair
- Expired products
- Stolen merchandise
- Sample/promotional use

**Required Fields**:
- quantity: Amount written off
- referenceId: Write-off authorization
- createdBy: Authorizing manager

**Example**:
```json
{
  "type": "WRITE_OFF",
  "quantity": 10,
  "referenceId": "WRITEOFF-2026-003",
  "createdBy": "user-manager-01",
  "notes": "Water damage from roof leak - insurance claim filed"
}
```

**Business Rules**:
- Requires manager approval
- Impacts financial statements
- Documented for insurance/tax purposes
- May trigger investigation

---

### Adjustment Movements (Corrections)

#### CYCLE_COUNT_ADJUSTMENT
**Description**: Correction based on physical inventory count

**Use Cases**:
- Regular cycle counting
- Annual physical inventory
- High-value item verification
- ABC analysis counting

**Required Fields**:
- quantity: Net adjustment (can be positive or negative)
- referenceId: Count sheet number
- createdBy: Counter/supervisor

**Example**:
```json
{
  "type": "CYCLE_COUNT_ADJUSTMENT",
  "quantity": -5,
  "referenceId": "COUNT-2026-W10-A12",
  "createdBy": "user-counter-03",
  "notes": "Physical count: 95, System count: 100, Variance: -5"
}
```

**Business Rules**:
- Requires count verification (double-blind)
- Large variances trigger investigation
- Updates accuracy metrics
- May require supervisor approval

---

#### DAMAGE_ADJUSTMENT
**Description**: Adjustment for damaged goods discovered

**Use Cases**:
- Damage found during put-away
- Damage discovered during picking
- Quality inspection failures
- Customer return damage

**Required Fields**:
- quantity: Amount damaged (always negative)
- referenceId: Incident report number
- createdBy: Discoverer/inspector

**Example**:
```json
{
  "type": "DAMAGE_ADJUSTMENT",
  "quantity": -3,
  "referenceId": "DAMAGE-REPORT-2026-045",
  "createdBy": "user-quality-inspector-02",
  "notes": "Forklift damage during put-away - 3 units destroyed"
}
```

**Business Rules**:
- Requires damage documentation
- May trigger insurance claim
- Routes damaged goods appropriately
- Updates safety/training metrics

---

#### SHRINKAGE
**Description**: Adjustment for unexplained inventory loss

**Use Cases**:
- Suspected theft
- Loss without clear cause
- Reconciliation adjustments
- Unexplained variances

**Required Fields**:
- quantity: Amount lost (always negative)
- referenceId: Investigation number
- createdBy: Investigator/manager

**Example**:
```json
{
  "type": "SHRINKAGE",
  "quantity": -2,
  "referenceId": "SHRINK-INV-2026-012",
  "createdBy": "user-loss-prevention-01",
  "notes": "Unexplained variance after cycle count - possible theft"
}
```

**Business Rules**:
- Requires investigation
- Tracked for loss prevention
- May trigger security review
- Impacts shrinkage KPIs

---

### Reservation Movements (Allocation)

#### RESERVATION
**Description**: Stock allocated to an order, not yet shipped

**Use Cases**:
- Order placement
- Pre-order allocation
- B2B contract fulfillment
- Marketplace committed inventory

**Required Fields**:
- quantity: Amount reserved
- referenceId: Order number
- createdBy: Order system

**Example**:
```json
{
  "type": "RESERVATION",
  "quantity": 3,
  "referenceId": "ORDER-2026-345678",
  "createdBy": "system-order-service",
  "notes": "Reserved for priority order - ship by 2026-03-05"
}
```

**Business Rules**:
- Reduces available quantity
- Does not reduce on-hand quantity
- May expire after timeout period
- Prevents overselling

---

#### RESERVATION_RELEASE
**Description**: Previously reserved stock released back to available

**Use Cases**:
- Order cancellation
- Reservation timeout
- Order modification
- Inventory reallocation

**Required Fields**:
- quantity: Amount released
- referenceId: Original order number
- createdBy: Order system

**Example**:
```json
{
  "type": "RESERVATION_RELEASE",
  "quantity": 3,
  "referenceId": "ORDER-2026-345678",
  "createdBy": "system-order-service",
  "notes": "Order cancelled by customer"
}
```

**Business Rules**:
- Must match existing reservation
- Increases available quantity
- May trigger automatic reallocation
- Updates order status

---

## Movement Data Model

### Core Fields

```java
StockMovement {
    id: UUID                    // Unique identifier
    inventoryItemId: UUID       // Parent inventory item
    type: StockMovementType     // Movement type enum
    quantity: int               // Absolute quantity (always positive)
    quantityBefore: int         // Stock level before movement
    quantityAfter: int          // Stock level after movement (calculated)
    referenceId: String         // External reference (PO, order, etc.)
    sourceLocationId: String    // Source location (for transfers)
    destinationLocationId: String // Destination location
    createdAt: Instant          // When movement occurred
    createdBy: UUID             // Who created the movement
    notes: String               // Additional context
}
```

### Calculated Fields

```java
// Quantity change calculation
quantityAfter = switch (type) {
    case PURCHASE_ORDER_RECEIPT, CUSTOMER_RETURN, TRANSFER_IN, 
         INITIAL_STOCK, RESERVATION_RELEASE
        -> quantityBefore + quantity
        
    case SALE, TRANSFER_OUT, RETURN_TO_VENDOR, WRITE_OFF, 
         RESERVATION
        -> quantityBefore - quantity
        
    case CYCLE_COUNT_ADJUSTMENT, DAMAGE_ADJUSTMENT, SHRINKAGE
        -> quantityBefore + quantity  // quantity can be negative
}

// Direction helper methods
boolean isInbound() {
    return type in [PURCHASE_ORDER_RECEIPT, CUSTOMER_RETURN, 
                    TRANSFER_IN, INITIAL_STOCK];
}

boolean isOutbound() {
    return type in [SALE, TRANSFER_OUT, RETURN_TO_VENDOR, WRITE_OFF];
}

boolean isAdjustment() {
    return type in [CYCLE_COUNT_ADJUSTMENT, DAMAGE_ADJUSTMENT, SHRINKAGE];
}

boolean isReservation() {
    return type in [RESERVATION, RESERVATION_RELEASE];
}
```

---

## API Operations

### Record Movement

```http
POST /api/inventories/{inventoryId}/movements
Content-Type: application/json

{
  "type": "PURCHASE_ORDER_RECEIPT",
  "quantity": 100,
  "referenceId": "PO-2026-001234",
  "createdBy": "user-123",
  "notes": "Delivery received in good condition"
}

Response: 201 Created
{
  "id": "mov-789",
  "inventoryItemId": "inv-456",
  "type": "PURCHASE_ORDER_RECEIPT",
  "quantity": 100,
  "quantityBefore": 50,
  "quantityAfter": 150,
  "referenceId": "PO-2026-001234",
  "createdAt": "2026-03-03T10:30:00Z",
  "createdBy": "user-123",
  "notes": "Delivery received in good condition"
}
```

### Get Movement History

```http
GET /api/inventories/{inventoryId}/movements

Response: 200 OK
{
  "movements": [
    {
      "id": "mov-789",
      "type": "PURCHASE_ORDER_RECEIPT",
      "quantity": 100,
      "quantityBefore": 50,
      "quantityAfter": 150,
      "createdAt": "2026-03-03T10:30:00Z"
    },
    {
      "id": "mov-790",
      "type": "SALE",
      "quantity": 10,
      "quantityBefore": 150,
      "quantityAfter": 140,
      "createdAt": "2026-03-03T11:00:00Z"
    }
  ],
  "total": 2
}
```

### Filter Movements

```http
# By type
GET /api/inventories/{id}/movements?type=SALE

# By date range
GET /api/inventories/{id}/movements?from=2026-01-01&to=2026-01-31

# By reference
GET /api/movements?referenceId=PO-2026-001234

# By user
GET /api/movements?createdBy=user-123

# Combined filters
GET /api/inventories/{id}/movements?type=CYCLE_COUNT_ADJUSTMENT&from=2026-01-01
```

---

## Reporting & Analytics

### Movement Summary Report
```sql
SELECT 
    type,
    COUNT(*) as movement_count,
    SUM(quantity) as total_quantity,
    AVG(quantity) as avg_quantity
FROM stock_movement
WHERE inventory_item_id = ? 
    AND created_at BETWEEN ? AND ?
GROUP BY type
ORDER BY movement_count DESC
```

### Variance Analysis
```sql
SELECT 
    DATE(created_at) as date,
    SUM(CASE WHEN type = 'CYCLE_COUNT_ADJUSTMENT' 
        THEN quantity ELSE 0 END) as variance
FROM stock_movement
WHERE inventory_item_id = ?
    AND type = 'CYCLE_COUNT_ADJUSTMENT'
GROUP BY DATE(created_at)
ORDER BY date DESC
```

### Velocity Calculation
```sql
SELECT 
    DATE(created_at) as date,
    SUM(CASE WHEN type = 'SALE' 
        THEN quantity ELSE 0 END) as units_sold
FROM stock_movement
WHERE inventory_item_id = ?
    AND created_at >= CURRENT_DATE - INTERVAL '90 days'
GROUP BY DATE(created_at)
ORDER BY date
```

---

## Business Rules & Validations

### General Rules
1. ✅ Movements are immutable - never updated after creation
2. ✅ Quantity must always be positive (sign determined by type)
3. ✅ Reference ID strongly recommended for audit trail
4. ✅ Created timestamp defaults to now, cannot be future
5. ✅ Movement must have valid inventory item
6. ✅ Type must be valid StockMovementType enum value

### Type-Specific Rules
1. ✅ TRANSFER movements require source/destination locations
2. ✅ RESERVATION must not exceed available quantity
3. ✅ WRITE_OFF requires authorization (manager role)
4. ✅ Large adjustments (>10% variance) require approval
5. ✅ INITIAL_STOCK limited to one per product-location

### Data Integrity
1. ✅ Quantity before/after automatically calculated
2. ✅ Movement order preserved by timestamp
3. ✅ Cascade delete with inventory item
4. ✅ Indexed for performance (inventory_id, created_at, type)

---

## Performance Considerations

### Query Optimization
- Index on `(inventory_item_id, created_at DESC)` for history queries
- Index on `reference_id` for cross-reference lookups
- Index on `type` for type-based filtering
- Consider partitioning by date for high-volume items

### Data Volume Management
- **Archival Strategy**: Move movements older than 2 years to archive table
- **Pagination**: Return max 100 movements per page
- **Compression**: Enable table compression for historical data
- **Monitoring**: Alert if single inventory has >10,000 movements

---

## Compliance & Audit

### Audit Trail Requirements
✅ WHO: Created by field tracks user  
✅ WHAT: Type and quantity show the change  
✅ WHEN: Created timestamp shows timing  
✅ WHY: Reference ID and notes provide context  
✅ HOW MUCH: Quantity before/after show impact  

### Regulatory Compliance
- **SOX (Sarbanes-Oxley)**: Immutable audit trail, approval workflows
- **FDA (Food & Drug)**: Lot tracking, expiration management, recall capability
- **Tax Authorities**: Valuation changes, COGS calculations, write-offs
- **Insurance**: Damage documentation, loss claims, shrinkage tracking

---

## Success Metrics

- **Audit Trail Completeness**: 100% of movements have reference ID
- **Movement Recording Latency**: < 100ms (p95)
- **Historical Query Performance**: < 200ms for 1 year of history
- **Data Accuracy**: Zero discrepancies between movements and quantity
- **Compliance**: Pass all regulatory audits with movement documentation

---

## Future Enhancements

### Phase 2
- Bulk movement import (CSV/API)
- Movement approval workflows
- Automated movement categorization
- Movement templates

### Phase 3
- Machine learning for anomaly detection
- Predictive shrinkage alerts
- Automated reconciliation suggestions
- Advanced analytics dashboard

### Phase 4
- Blockchain integration for immutability
- IoT device integration (scales, scanners)
- Real-time movement streaming
- Mobile movement recording

---

## Related Documentation

- [Inventory Management Feature](./inventory-management.md)
- [ADR-005: Inventory Domain Design](../architecture/decisions/ADR-005-inventory-domain-design.md)
- [ADR-006: JPA Assembler Pattern](../architecture/decisions/ADR-006-inventory-jpa-assembler-pattern.md)
- API Documentation: `/api/docs`
