# Feature: Bin Management

## 1. Problem Statement

Zones within a warehouse or store need further sub-division into physical or logical storage positions (bins) so that operators can track exactly where items are placed, enforce capacity limits, and manage stock at the most granular level. Bin management provides CRUD lifecycle operations for these storage positions scoped to their parent zone.

---

## 2. Business Scope

### In scope
- Create bins within a zone
- Update bin metadata (code, name, max capacity)
- Activate and deactivate bins
- Delete bins
- List bins by zone with optional active filter (paginated)
- Get a single bin by ID
- Unique bin code within a zone
- Optional max-capacity field per bin
- Zone deactivation cascades to deactivate all active bins within the zone (via event listener)

### Out of scope
- Zone or location management (separate features)
- Inventory assignment at the bin level
- Moving bins between zones
- Bin capacity enforcement during stock placement (handled by inventory management)

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Bin
- Type: Aggregate Root

### Related concepts

| Name          | Type          | Notes                                         |
|---------------|---------------|-----------------------------------------------|
| Zone          | Aggregate     | Parent scope for bin code uniqueness           |
| ZoneId        | Value Object  | Bin's owning zone identity                     |
| BinId         | Value Object  | Identity                                       |
| maxCapacity   | Integer       | Optional capacity limit (null = unlimited)     |

---

## 4. Aggregate Boundary

### Aggregate Root
- Bin

### Inside boundary
- ZoneId (Value Object — reference to parent)
- Status (active / inactive)
- maxCapacity (optional Integer)

### Outside boundary (reference only)
- Zone (parent aggregate)
- InventoryItem (future consumer)

---

## 5. Invariants

- Bin code must be unique within a zone
- Bin must belong to an existing zone
- Code is required (not blank)
- A bin starts as active upon creation
- Activate is idempotent (already-active stays active)
- Deactivate is idempotent (already-inactive stays inactive)
- Zone deactivation cascades to deactivate all active bins within it (via `ZoneDeactivationEventListener`)
- Deletion removes the bin permanently (hard delete)

---

## 6. Domain Operations

| Operation   | Description                          | Enforces Invariants                       |
|-------------|--------------------------------------|-------------------------------------------|
| create      | Create bin within a zone             | zone existence, code uniqueness, required fields |
| update      | Update code, name, maxCapacity       | code uniqueness on change                 |
| activate    | Activate a bin                       | bin existence                             |
| deactivate  | Deactivate a bin                     | bin existence                             |
| delete      | Hard-delete a bin                    | bin existence                             |

---

## 7. Domain Model (Changes)

### Bin

```java
public class Bin extends AggregateRoot<Id> {
    private final Id zoneId;      // owning zone (immutable)
    private String code;          // unique within zone
    private String name;          // optional display name
    private Integer maxCapacity;  // optional capacity limit
    private boolean active;       // status flag
}
```

---

## 8. Creation vs Reconstitution

### Creation
- Method: `Bin.create(id, zoneId, code, name, maxCapacity)`
- Validations:
    - Zone exists (enforced in command handler)
    - Code is unique within the zone (enforced in command handler)
    - Code is not null (`Objects.requireNonNull`)
    - ZoneId is not null (`Objects.requireNonNull`)
    - Active defaults to `true`
    - Emits `BinCreatedEvent`

### Reconstitution
- Constructor: `new Bin(id, zoneId, code, name, maxCapacity, active)`
- Assumptions:
    - Persisted data is already valid
    - No events emitted

---

## 9. Persistence & Mapping Strategy

### Repository interfaces
- `BinRepository` (domain): `findById`, `findByCodeAndZoneId`, `save`, `delete`, `existsByCodeAndZoneId`, `existsByZoneId`
- `BinQueryRepository` (read): `queryByZoneId`, `queryByZoneIdAndActive`

### JPA repository
- `BinJpaRepository`: `findByUuid`, `findByCodeAndZoneId`, `existsByCodeAndZoneId`, `existsByZoneId`, `findAllByZoneId` (pageable), `findAllByZoneIdAndActive` (pageable), `findAllByActive` (pageable)

### Assembler
- `BinJpaAssembler` (interface) → `BinJpaAssemblerImpl`
- `BinEntityMapper`: maps domain fields to JPA entity (via MapStruct)
- `BinMapper`: maps JPA entity back to domain (via MapStruct)

### Schema

```sql
CREATE TABLE bin (
    id            BIGSERIAL PRIMARY KEY,
    uuid          VARCHAR(36)  NOT NULL UNIQUE,
    code          VARCHAR(100) NOT NULL,
    name          VARCHAR(255),
    max_capacity  INTEGER,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    zone_id       VARCHAR(36)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_bin_code_zone ON bin(code, zone_id);
CREATE INDEX idx_bin_zone ON bin(zone_id);
```

---

## 10. Application Services

### Commands

| Command                   | Handler                          | Description                                  |
|---------------------------|----------------------------------|----------------------------------------------|
| `CreateBinCommand`        | `CreateBinCommandHandler`        | Creates a bin within a zone                  |
| `UpdateBinCommand`        | `UpdateBinCommandHandler`        | Updates bin metadata and/or active status     |
| `ActivateBinCommand`      | `ActivateBinCommandHandler`      | Activates a bin                              |
| `DeactivateBinCommand`    | `DeactivateBinCommandHandler`    | Deactivates a bin                            |
| `DeleteBinCommand`        | `DeleteBinCommandHandler`        | Hard-deletes a bin                           |

### Queries

| Query                       | Handler                              | Description                          |
|-----------------------------|--------------------------------------|--------------------------------------|
| `GetBinQuery`               | `GetBinQueryHandler`                 | Get bin by ID                        |
| `ListBinsByZoneQuery`       | `ListBinsByZoneQueryHandler`         | List bins by zone (paginated)        |

---

## 11. API Endpoints

| Method | Endpoint                                    | Auth Header       | Request Body           | Response                                    |
|--------|---------------------------------------------|-------------------|------------------------|---------------------------------------------|
| POST   | `/api/v1/inventory/bins`                    | `X-Actor-Id` (opt)| `CreateBinRequest`     | `201` `EntityModel<BinResponse>`            |
| PATCH  | `/api/v1/inventory/bins/{binId}`            | `X-Actor-Id` (opt)| `UpdateBinRequest`     | `200` `EntityModel<BinResponse>`            |
| PATCH  | `/api/v1/inventory/bins/{binId}/activate`   | `X-Actor-Id` (opt)| —                      | `200` `EntityModel<BinResponse>`            |
| PATCH  | `/api/v1/inventory/bins/{binId}/deactivate` | `X-Actor-Id` (opt)| —                      | `200` `EntityModel<BinResponse>`            |
| DELETE | `/api/v1/inventory/bins/{binId}`            | `X-Actor-Id` (opt)| —                      | `204` No Content                            |
| GET    | `/api/v1/inventory/bins/{binId}`            | —                 | —                      | `200` `EntityModel<BinResponse>`            |
| GET    | `/api/v1/inventory/bins/zones/{zoneId}`     | —                 | Query params           | `200` `PagedModel<EntityModel<BinResponse>>`|

### Query parameters for list endpoint

| Parameter | Type    | Required | Description                       |
|-----------|---------|----------|-----------------------------------|
| `active`  | Boolean | No       | Filter by active/inactive status  |
| `page`    | int     | No       | Page index (default 0)            |
| `size`    | int     | No       | Page size (default 20)            |

### Request/Response schemas

**CreateBinRequest:**
```json
{
  "zoneId": "zone-uuid-1",
  "code": "BIN-A1",
  "name": "Shelf A Row 1",
  "maxCapacity": 100
}
```

**UpdateBinRequest:**
```json
{
  "code": "BIN-A1-UPDATED",
  "name": "Shelf A Row 1 Updated",
  "maxCapacity": 150,
  "active": true
}
```

**BinResponse:**
```json
{
  "id": "bin-uuid-1",
  "zoneId": "zone-uuid-1",
  "code": "BIN-A1",
  "name": "Shelf A Row 1",
  "maxCapacity": 100,
  "active": true
}
```

---

## 12. Integration & Side Effects

### Domain Events

| Event                   | When Published                        | Consumers                                    |
|-------------------------|---------------------------------------|----------------------------------------------|
| `BinCreatedEvent`       | Bin is created                        | Audit log                                    |
| `BinUpdatedEvent`       | Bin metadata is updated               | Audit log                                    |
| `BinActivatedEvent`     | Bin is activated                      | Audit log                                    |
| `BinDeactivatedEvent`   | Bin is deactivated                    | Audit log                                    |
| `BinDeletedEvent`       | Bin is deleted                        | Audit log                                    |
| `BinChangedEvent`       | Bin is added, updated, or removed     | Cross-module notification (ADDED, UPDATED, REMOVED) |

### Cross-module effects
- Zone deactivation cascades to deactivate all active bins within the zone via `ZoneDeactivationEventListener` (listens to `ZoneDeactivatedEvent`, dispatches `DeactivateBinCommand` per active bin)
- Zone deletion checks for dependent bins before allowing removal (`ZoneHasDependentBins` error)
- Bin deactivation may affect inventory placement and picking workflows

---

## 13. Error Scenarios

| Scenario                              | HTTP Status | Error Detail                           | Error Code                         |
|---------------------------------------|-------------|----------------------------------------|------------------------------------|
| Bin not found                         | 404         | `Bin not found`                        | `inv.service.bin.not_found`        |
| Zone not found                        | 404         | `Zone not found`                       | `inv.service.zone.not_found`       |
| Bin code already exists in zone       | 409         | `Bin code already in use`              | `inv.service.bin.already_exists`   |
| Unable to add bin (internal failure)  | 500         | `Unable to add bin`                    | `inv.service.bin.add_failed`       |
| Missing required fields               | 400         | Validation errors (code)               | —                                  |

---

## 14. Implementation Order

1. Bin domain aggregate (`Bin.java`)
2. Domain events: `BinCreatedEvent`, `BinUpdatedEvent`, `BinActivatedEvent`, `BinDeactivatedEvent`, `BinDeletedEvent`, `BinChangedEvent`
3. `BinRepository` interface (domain) and `BinQueryRepository` interface (read)
4. `BinJpaAssembler`, `BinEntityMapper`, `BinMapper` (MapStruct)
5. `BinEntity` (JPA), `BinJpaRepository`, and `DefaultBinRepository`
6. CQRS commands: `CreateBinCommand`, `UpdateBinCommand`, `ActivateBinCommand`, `DeactivateBinCommand`, `DeleteBinCommand`
7. CQRS command handlers: `CreateBinCommandHandler`, `UpdateBinCommandHandler`, `ActivateBinCommandHandler`, `DeactivateBinCommandHandler`, `DeleteBinCommandHandler`
8. CQRS queries: `GetBinQuery`, `ListBinsByZoneQuery`
9. CQRS query handlers: `GetBinQueryHandler`, `ListBinsByZoneQueryHandler`
10. Request/Response DTOs: `CreateBinRequest`, `UpdateBinRequest`, `BinResponse`
11. Request mappers: `CreateBinRequestMapper`, `UpdateBinRequestMapper`, `ActivateBinRequestMapper`, `DeactivateBinRequestMapper`, `DeleteBinRequestMapper`, `GetBinRequestMapper`, `ListBinsRequestMapper`
12. `BinCommandService` and `BinQueryService`
13. `BinController` (REST endpoints)
14. `BinModelAssembler` (HATEOAS)
15. `ZoneDeactivationEventListener` (bin cascade on zone deactivation)
16. Tests: domain unit, repository integration, controller integration

---

## 15. Definition of Done

- [x] All invariants enforced in domain
- [x] Repository implementation complete with event publishing
- [x] All REST endpoints implemented (create, update, activate, deactivate, delete, get, list)
- [x] Request validation in place
- [x] Bin code uniqueness enforced within parent zone
- [x] Error scenarios handled with appropriate HTTP status codes
- [x] Domain events published on lifecycle changes
- [x] Zone deactivation cascades to deactivate active bins
- [x] HATEOAS links provided (self, paged-bins, edit-bin, delete-bin, activate/deactivate)
- [x] Tests cover domain, repository, and API endpoints
