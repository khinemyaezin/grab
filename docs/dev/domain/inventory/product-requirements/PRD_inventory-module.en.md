# Product Requirements Document: Inventory Module

## 1. Summary

This PRD defines the inventory module scope for the commerce platform.

The inventory module is the source of truth for stock availability. It must
help the platform track seller-owned stock, manage stock by location, reserve
stock for orders, and prevent overselling.

## 2. Problem

An ecommerce platform needs clear and accurate inventory control so sellers can
offer products confidently and customers can order only what is actually
available.

Without this module:

- stock can be oversold
- sellers cannot manage stock across locations
- checkout cannot reserve stock safely
- cancellations cannot release stock correctly
- stock changes are difficult to trace

## 3. Users

- Platform admin
- Seller or operations user
- Order and checkout workflows that need stock availability and reservation

## 4. Goals

- Keep accurate stock by seller and by location
- Provide reliable product availability for checkout
- Reserve stock during the order flow
- Prevent overselling
- Maintain a clear record of stock changes

## 5. In Scope

The inventory module must include:

- seller-owned inventory records
- inventory by product variant and location
- stock availability tracking (on-hand, reserved, in-transit, damaged)
- stock reservation and release
- stock deduction after order completion
- stock receiving, adjustment, write-off, and damage marking
- inventory history and audit trail
- warehouse or seller-managed location management with zone and bin hierarchy
- inventory status lifecycle management
- multi-location inventory allocation
- reorder configuration and low-stock alerts
- idempotency and concurrency controls
- oversell prevention rules

## 6. Out of Scope

The inventory module does not include:

- delivery management
- shipment tracking integrations
- supplier purchasing workflows
- forecasting and demand planning
- promotions and pricing logic
- customer-facing order management
- payment processing

## 7. Main Features

### 7.1 Inventory Records

The module must support an inventory record for a seller, a product variant,
and a stock location.

Each record must identify:

- seller
- product variant
- location
- stock quantities needed for availability and reservation

### 7.2 Stock Availability

The module must track stock in a way that supports online selling and warehouse
operations.

It must support the following quantity dimensions:

- **on-hand**: physical stock physically present at the location
- **reserved**: stock allocated to pending orders or reservations
- **in-transit**: stock that is incoming but not yet received at the location
- **damaged**: stock that is physically present but unusable or unsellable
- **available**: calculated as `on-hand - reserved - damaged` (minimum zero)

Available stock must always be calculated from the inventory record and must be
usable by storefront, checkout, and order workflows. The available quantity
represents what can actually be sold or committed to new orders.

### 7.3 Reservation Management

The module must support reservation as part of the order flow.

It must:

- reserve stock during checkout or order confirmation
- release stock when an order is cancelled or expires
- deduct reserved stock when the order reaches its completion point
- prevent reservation when available stock is not enough

### 7.4 Stock Operations

The module must support the main stock changes needed by sellers and operators.

It must support:

- **receiving stock**: adding inbound stock from purchase orders, customer returns,
  inter-location transfers, or initial stock loading
- **adjusting stock**: correcting stock levels due to cycle counts, shrinkage,
  found items, or other discrepancies
- **deducting stock**: removing stock from confirmed sales or completed orders
- **releasing reserved stock**: returning reserved stock to availability when
  orders are cancelled or reservations expire
- **writing off stock**: permanently removing stock that is lost, stolen, expired,
  or otherwise unrecoverable
- **marking stock as damaged**: moving stock from sellable to damaged quantity
  when items are found to be defective or unsellable

All stock operations must create an immutable stock movement record as part of
the audit trail.

### 7.5 Inventory History

The module must keep a clear audit trail of inventory changes.

It must record stock movements and reservation changes so platform users can
trace why stock increased, decreased, or became unavailable.

### 7.6 Location Management

The module must support inventory locations and their internal topology used by
sellers and the platform. Locations represent physical places where stock is held,
and each location can be subdivided into zones and bins for warehouse organization.

#### 7.6.1 Location

The module must support:

- warehouse or store locations with a unique code, name, type, and address
- location types: `WAREHOUSE` and `STORE`
- active and inactive location status
- location creation, update, activation, and deactivation
- rules that block inventory creation or stock operations against inactive
  or non-existent locations
- location lookup by ID, by unique code, and listing with filters (active, type)

#### 7.6.2 Zones

Each location can contain multiple zones that represent functional areas within
the warehouse or store.

The module must support:

- zone types: `PICKING`, `STORAGE`, `STAGING`, `RETURNS`, `DAMAGED`, `RECEIVING`
- zone creation, update, and removal within a location
- active and inactive zone status
- unique zone code within a location
- adding, updating, and removing zones through the location lifecycle

#### 7.6.3 Bins

Each zone can contain multiple bins that represent specific storage positions.

The module must support:

- bin creation, update, and removal within a zone
- bin code, name, and maximum capacity
- active and inactive bin status
- unique bin code within a zone

#### 7.6.4 Location Lifecycle Rules

- a location must exist and be active before inventory can be assigned to it
- zone and bin codes must be unique within their parent scope
- location deactivation must consider dependent inventory records and apply
  policy-based checks before allowing the transition
- location lifecycle changes must emit domain events for audit and integration

### 7.7 Oversell Prevention

The module must enforce rules that protect stock accuracy.

It must:

- block stock from going below allowed limits
- block reservation beyond available stock
- block deduction beyond reserved or available stock
- ensure the same stock is not allocated twice

### 7.8 Inventory Status Lifecycle

The module must support a defined lifecycle for inventory items that reflects
their operational state.

It must support the following statuses:

- **ACTIVE**: inventory item is operational and stock is sellable
- **OUT_OF_STOCK**: automatically set when available quantity reaches zero;
  automatically reverted to ACTIVE when stock is replenished
- **SUSPENDED**: manually set to temporarily halt selling without deleting the
  inventory record
- **DISCONTINUED**: permanently retired; no further stock operations allowed

Status transitions must be enforced:

- ACTIVE and OUT_OF_STOCK transitions are automatic based on available quantity
- SUSPENDED and DISCONTINUED are manual transitions initiated by operators
- stock operations must be blocked on SUSPENDED or DISCONTINUED items

### 7.9 Inventory Allocation

The module must support allocation of stock across multiple locations to fulfill
orders efficiently.

It must:

- find available inventory for a given SKU across all active locations
- reserve stock from one or more locations to satisfy an order
- return an allocation result indicating which locations fulfilled the order
- fail with a clear error when total available stock across all locations is
  insufficient

### 7.10 Reorder Service

The module must support reorder configuration and low-stock alerting to help
sellers maintain adequate stock levels.

Each inventory item can have a reorder configuration with:

- **safety stock**: minimum buffer stock to maintain
- **reorder point**: stock level that triggers a reorder alert
- **reorder quantity**: recommended quantity to order when reorder point is reached
- **max stock**: upper limit for stock at the location

The module must:

- evaluate reorder thresholds after stock changes
- emit a low-stock alert event when available quantity drops below the reorder point
- provide reorder recommendations based on the configured reorder quantity

### 7.11 Idempotency and Concurrency

The module must protect stock operations from duplicate processing and
concurrent conflicts.

It must support:

- **idempotency keys** on reservation operations so that retrying the same
  request does not create duplicate reservations
- **optimistic locking** on inventory records to prevent lost updates when
  concurrent operations modify the same inventory item

### 7.12 Stock Movement Types

The module must define a comprehensive taxonomy of stock movement types to
categorize all inventory changes for audit and reporting.

Movement types are categorized by direction:

**Inbound movements** (stock increases):

- purchase order receipt
- customer return
- inter-location transfer in
- initial stock loading

**Outbound movements** (stock decreases):

- sale or order fulfillment
- inter-location transfer out
- return to vendor
- write-off

**Adjustment movements** (stock corrections):

- cycle count adjustment
- damage adjustment
- shrinkage

**Reservation movements** (allocation changes):

- reservation
- reservation release

Every stock operation must produce exactly one immutable stock movement record
with the appropriate type.

## 8. Business Rules

- Inventory must belong to a seller, a product variant, and a location.
- The combination of SKU and location must be unique; duplicate inventory records
  for the same variant at the same location are not allowed.
- Available stock must be calculated as `on-hand - reserved - damaged` and must
  never be negative.
- Inventory availability must be calculated consistently across the platform.
- A location must exist and be active before stock can be assigned or reserved there.
- Zone codes must be unique within a location; bin codes must be unique within a zone.
- Reservation must reduce sellable availability immediately.
- Release must return stock to availability immediately.
- Final stock deduction must happen only through approved order lifecycle steps.
- Every stock mutation must produce an immutable stock movement record for audit.
- Stock movements are append-only; corrections require compensating movements.
- Inventory status must auto-transition to OUT_OF_STOCK when available quantity
  reaches zero, and back to ACTIVE when stock is replenished.
- Stock operations must be blocked on SUSPENDED or DISCONTINUED inventory items.
- Reservation operations must be idempotent; duplicate requests with the same
  idempotency key must not create duplicate reservations.
- Concurrent modifications to the same inventory record must be protected by
  optimistic locking.
- Inventory history must be traceable for business review and audit needs.
- Location lifecycle events must be published for audit and downstream integration.

## 9. Success Criteria

The inventory module is successful when:

- sellers and admins can manage stock by location
- operators can manage the full location topology (zones and bins) through the API
- checkout can reserve stock safely with idempotency protection
- cancelled or expired orders release stock correctly
- overselling is prevented across all locations
- stock history can be reviewed clearly with a complete, immutable audit trail
- the platform can show accurate product availability including damaged and
  in-transit stock
- inventory status transitions automatically based on stock levels
- low-stock alerts are triggered when inventory drops below reorder thresholds
- multi-location allocation can fulfill orders from available stock across warehouses
- concurrent stock operations are handled without data loss or duplication
- location lifecycle changes are traceable through domain events

## 10. Dependencies

The inventory module depends on the wider platform for:

- seller profile support
- catalog and product variant support
- checkout and order lifecycle support
- admin and permissions support

## 11. Related Documents

- `docs/BRD/commerce-platform-brd.md`
- `docs/PRD/catalog-module-prd.md`
