# Feature: Zone Management

## 1. Problem Statement

Warehouses and stores need internal organization beyond the location level. Zones represent functional areas within a location (e.g., picking, storage, staging, returns) so that operators can organize inventory placement, optimize picking routes, and manage stock movement within a facility.

---

## 2. Business Scope

### In scope
- Create zones within a location
- Update zone metadata (code, name, type)
- Activate and deactivate zones
- List zones by location with optional active filter
- Zone types: `PICKING`, `STORAGE`, `STAGING`, `RETURNS`, `DAMAGED`, `RECEIVING`
- Unique zone code within a location
- Cascade deactivation of bins when a zone is deactivated

### Out of scope
- Bin management (separate sub-feature)
- Location creation or management
- Moving zones between locations
- Inventory assignment at the zone level

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Zone
- Type: Aggregate Root

### Related concepts

| Name          | Type          | Notes                                    |
|---------------|---------------|------------------------------------------|
| ZoneType      | Enum          | PICKING, STORAGE, STAGING, RETURNS, DAMAGED, RECEIVING |
| Location      | Aggregate     | Parent scope for zone code uniqueness    |
| Bin           | Aggregate     | Child of zone (cascade deactivation)     |
| LocationId    | Value Object  | Zone's owning location identity           |
| ZoneId        | Value Object  | Identity                                  |

---

## 4. Aggregate Boundary

### Aggregate Root
- Zone

### Inside boundary
- LocationId (Value Object — reference to parent)
- Status (active / inactive)

### Outside boundary (reference only)
- Location (parent aggregate)
- Bin (child aggregate, eventual consistency via events)

---

## 5. Invariants

- Zone code must be unique within a location
- Zone must belong to an existing location
- Name is required
- Type is required
- A zone starts as active upon creation
- Activate is idempotent (already-active stays active)
- Deactivate is idempotent (already-inactive stays inactive)
- Deactivating a zone cascades to deactivate all active bins within it

---

## 6. Domain Operations

| Operation   | Description                          | Enforces Invariants              |
|-------------|--------------------------------------|----------------------------------|
| create      | Create zone within a location        | location existence, code uniqueness, required fields |
| update      | Update code, name, type, active flag | code uniqueness on change        |
| activate    | Activate a zone                      | zone existence                   |
| deactivate  | Deactivate a zone                    | zone existence, bin cascade      |

---

## 7. Domain Model (Changes)

### Zone

```java
public class Zone extends AggregateRoot<Id> {
    private Id locationId;      // owning location
    private String code;        // unique within location
    private String name;        // required
    private ZoneType type;      // PICKING, STORAGE, etc.
    private boolean active;     // status flag
}
```

### ZoneType (enum)

```java
public enum ZoneType {
    PICKING,
    STORAGE,
    STAGING,
    RETURNS,
    DAMAGED,
    RECEIVING
}
```

---

## 8. Creation vs Reconstitution

### Creation
- Method: `Zone.create(id, locationId, code, name, type)`
- Validations:
    - Location exists
    - Code is unique within the location
    - Code is not blank
    - Name is not blank
    - Type is not null
    - Active defaults to `true`
    - Emits `ZoneCreatedEvent`

### Reconstitution
- Constructor: `new Zone(id, locationId, code, name, type, active)`
- Assumptions:
    - Persisted data is already valid
    - No events emitted

---

## 9. Persistence & Mapping Strategy

### Repository interfaces
- `ZoneRepository` (domain): `findById`, `save`, `delete`, `existsByCodeAndLocationId`
- `ZoneQueryRepository` (read): `queryByLocationId`, `queryByLocationIdAndActive`

### JPA repository
- `ZoneJpaRepository`: `findByUuid`, `findAllByLocationIdAndActive`, `existsByCodeAndLocationId`, `findAllByLocationId` (pageable), `findAllByType`, `findAllByActive`

### Assembler
- `ZoneJpaAssembler` (interface) → `ZoneJpaAssemblerImpl`
- `ZoneEntityMapper`: maps domain fields to JPA entity (via MapStruct)
- `ZoneMapper`: maps JPA entity back to domain (with `@AfterMapping` for active flag)

### Schema

```sql
CREATE TABLE zone (
    id          BIGSERIAL PRIMARY KEY,
    uuid        VARCHAR(36)  NOT NULL UNIQUE,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    location_id VARCHAR(36)  NOT NULL
);

CREATE UNIQUE INDEX idx_zone_code_location ON zone(code, location_id);
CREATE INDEX idx_zone_location ON zone(location_id);
```

---

## 10. Application Services

### Commands

| Command                   | Handler                          | Description                                  |
|---------------------------|----------------------------------|----------------------------------------------|
| `CreateZoneCommand`       | `CreateZoneCommandHandler`       | Creates a zone within a location             |
| `UpdateZoneCommand`       | `UpdateZoneCommandHandler`       | Updates zone metadata and/or active status   |
| `ActivateZoneCommand`     | `ActivateZoneCommandHandler`     | Activates a zone                             |
| `DeactivateZoneCommand`   | `DeactivateZoneCommandHandler`   | Deactivates a zone (cascades to bins)        |

### Queries

| Query                       | Handler                              | Description                        |
|-----------------------------|--------------------------------------|------------------------------------|
| `GetZoneQuery`              | `GetZoneQueryHandler`                | Get zone by ID                     |
| `ListZonesByLocationQuery`  | `ListZonesByLocationQueryHandler`    | List zones by location (paginated) |

---

## 11. API Endpoints

| Method | Endpoint                               | Auth Header       | Request Body               | Response                          |
|--------|----------------------------------------|-------------------|----------------------------|-----------------------------------|
| POST   | `/api/v1/zones/locations/{locationId}` | `X-Actor-Id` (opt)| `CreateZoneRequest`        | `201` `EntityModel<ZoneResponse>` |
| PATCH  | `/api/v1/zones/{zoneId}`               | `X-Actor-Id` (opt)| `UpdateZoneRequest`        | `200` `EntityModel<ZoneResponse>` |
| POST   | `/api/v1/zones/{zoneId}/activate`      | `X-Actor-Id` (opt)| —                          | `200` `EntityModel<ZoneResponse>` |
| POST   | `/api/v1/zones/{zoneId}/deactivate`    | `X-Actor-Id` (opt)| —                          | `200` `EntityModel<ZoneResponse>` |
| GET    | `/api/v1/zones/locations/{locationId}` | —                 | Query params               | `200` `PagedModel<EntityModel<ZoneResponse>>` |

### Query parameters for list endpoint

| Parameter | Type    | Required | Description                     |
|-----------|---------|----------|---------------------------------|
| `active`  | Boolean | No       | Filter by active/inactive status |
| `page`    | int     | No       | Page index (default 0)          |
| `size`    | int     | No       | Page size (default 20)          |

### Request/Response schemas

**CreateZoneRequest:**
```json
{
  "code": "ZONE-P1",
  "name": "Picking Zone A",
  "type": "PICKING"
}
```

**UpdateZoneRequest:**
```json
{
  "code": "ZONE-P1-UPDATED",
  "name": "Picking Zone Alpha",
  "type": "STORAGE",
  "active": true
}
```

**ZoneResponse:**
```json
{
  "id": "zone-uuid-1",
  "locationId": "loc-uuid-1",
  "code": "ZONE-P1",
  "name": "Picking Zone A",
  "type": "PICKING",
  "active": true
}
```

---

## 12. Integration & Side Effects

### Domain Events

| Event                   | When Published                        | Consumers                                    |
|-------------------------|---------------------------------------|----------------------------------------------|
| `ZoneCreatedEvent`      | Zone is created                       | Audit log                                    |
| `ZoneUpdatedEvent`      | Zone metadata is updated              | Audit log                                    |
| `ZoneActivatedEvent`    | Zone is activated                     | Audit log                                    |
| `ZoneDeactivatedEvent`  | Zone is deactivated                   | `ZoneDeactivationEventListener` → cascades to deactivate all active bins in the zone |

### Cross-module effects
- Deactivating a zone deactivates all active bins within it via eventual consistency (event listener dispatches `DeactivateBinCommand` per bin)
- Zone deactivation may affect inventory placement and picking workflows

---

## 13. Error Scenarios

| Scenario                          | HTTP Status | Error Detail                         | Error Code                        |
|-----------------------------------|-------------|--------------------------------------|-----------------------------------|
| Zone not found                    | 404         | `Zone not found`                     | `inv.service.zone.not_found`     |
| Location not found                | 404         | `Location not found`                 | `inv.service.location.not_found` |
| Zone code already exists in location | 409      | `Zone code already in use`           | `inv.service.zone.already_exists` |
| Missing required fields           | 400         | Validation errors (code, name, type) | —                                 |
| Invalid zone type                 | 400         | `Invalid zone type`                  | —                                 |

---

## 14. Implementation Order

1. Zone domain aggregate and ZoneType enum
2. Domain events: ZoneCreatedEvent, ZoneUpdatedEvent, ZoneActivatedEvent, ZoneDeactivatedEvent
3. ZoneRepository interface and ZoneQueryRepository interface
4. ZoneJpaAssembler, ZoneEntityMapper, ZoneMapper
5. ZoneJpaRepository and DefaultZoneRepository
6. CQRS commands: Create, Update, Activate, Deactivate
7. CQRS queries: GetZone, ListZonesByLocation
8. Request/Response DTOs and request mappers
9. ZoneCommandService and ZoneQueryService
10. ZoneController (REST endpoints)
11. ZoneDeactivationEventListener (bin cascade)
12. ZoneModelAssembler (HATEOAS)
13. Tests: domain unit, repository integration, service unit, controller integration

---

## 15. Definition of Done

- [ ] All invariants enforced in domain
- [ ] Repository implementation complete with event publishing
- [ ] All REST endpoints implemented (create, update, activate, deactivate, list)
- [ ] Request validation in place
- [ ] Zone code uniqueness enforced within parent location
- [ ] Error scenarios handled with appropriate HTTP status codes
- [ ] Domain events published on lifecycle changes
- [ ] Zone deactivation cascades to deactivate active bins
- [ ] Tests cover domain, repository, application services, and API endpoints
