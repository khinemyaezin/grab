# ADR-008: Complete Inventory Location Module as a First-Class Capability

## Status
Proposed (March 5, 2026)

## Context

The inventory domain already models physical topology:

- `Location` aggregate (`inventory-domain`)
- nested `Zone` and `Bin` entities
- JPA persistence for location hierarchy (`inventory-infrastructure`)

However, the application layer currently exposes only inventory-item operations. There is no location management workflow in the store module:

- no location/zone/bin commands or queries
- no REST endpoints for location lifecycle
- no location module tests (domain or infrastructure coverage is minimal)

Inventory creation currently accepts any `locationId` and does not verify existence/active status before persisting an `InventoryItem`.

## Decision

Treat Inventory Location as a first-class sub-capability inside the Inventory bounded context and complete it end-to-end.

### 1. Add Application Use Cases for Location Lifecycle

Implement command/query flows for:

- create/update/deactivate/activate location
- add/update/remove zone
- add/update/remove bin
- get location by id/code
- list locations (all, active, by type)

### 2. Enforce Application-Level Referential Integrity

Before creating inventory for a `locationId`, validate:

- location exists
- location is active

Reject invalid references with typed domain/application errors.

### 3. Define Safe Deactivation/Deletion Rules

Use soft-state (`active`) as the default lifecycle strategy.

- deactivation allowed with business checks
- physical delete only for controlled admin/maintenance cases
- block deactivation/delete when active inventory still depends on the location (unless explicitly forced by policy)

### 4. Add Location Domain Events

Introduce location lifecycle events for audit/integration:

- `LocationCreated`
- `LocationUpdated`
- `LocationDeactivated`
- `ZoneChanged`
- `BinChanged`

Publish through the module outbox pattern already adopted in ADR-007.

### 5. Add Test Coverage as a Release Gate

Require tests across:

- location command handlers
- location query handlers
- location repository mapping/graph synchronization
- API integration (happy path + validation failures)

## Consequences

### Positive

- closes a current capability gap between domain model and exposed API
- prevents invalid inventory rows that reference non-existent/inactive locations
- makes warehouse topology manageable through official workflows
- improves traceability and integration readiness via explicit events

### Negative

- adds new handlers/endpoints and validation branches
- introduces migration and backward-compatibility work for existing clients
- requires additional test maintenance

## Alternatives Considered

### Keep Location as Persistence-Only Internal Model

Do not expose location lifecycle and continue passing raw `locationId` into inventory creation.

Why rejected:

- leaves integrity checks to external callers
- keeps core topology unmanaged in the system of record
- increases risk of orphan or invalid inventory references

## Implementation Notes

Recommended delivery order:

1. Add validation to inventory creation (`locationId` must exist + active)
2. Add location query API (read-only)
3. Add location command API (write)
4. Add location events + outbox wiring
5. Add comprehensive tests
