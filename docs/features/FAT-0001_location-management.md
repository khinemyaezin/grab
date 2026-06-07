# Feature: Location Management

## 1. Problem Statement

Sellers and platform operators need to manage physical locations (warehouses and stores) where inventory is held. Each location must be identifiable, addressable, and have a lifecycle so that inventory operations can be scoped correctly and stock can be allocated to the right place.

---

## 2. Business Scope

### In scope
- Create, update, activate, and deactivate locations
- Location types: `WAREHOUSE` and `STORE`
- Lookup location by ID, by unique code
- List locations with pagination and filters (seller, active status, type)
- Location address management
- Retrieve seller id from (`X-Actor-Id`) which is required

### Out of scope
- Zone and bin management (separate feature)
- Inventory assignment or stock operations scoped to locations
- Location deletion (only deactivation supported)

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Location
- Type: Aggregate Root

### Related concepts

| Name           | Type          | Notes                                     |
|----------------|---------------|-------------------------------------------|
| LocationType   | Enum          | WAREHOUSE, STORE                          |
| Address        | Value Object  | Embedded in Location                      |
| LocationId     | Value Object  | Identity                                   |

---

## 4. Aggregate Boundary

### Aggregate Root
- Location

### Inside boundary
- Address (Value Object)
- Status (active / inactive)

### Outside boundary (reference only)
- InventoryItem
- Zone / Bin

---

## 5. Invariants

- Code must be unique across all locations
- Name is required
- Address country is required
- Location must exist and be active before inventory can be assigned to it
- Activation is idempotent (already-active stays active)
- Deactivation is idempotent (already-inactive stays inactive)
- Location type is immutable after creation

---

## 6. Domain Operations

| Operation      | Description                          | Enforces Invariants         |
|----------------|--------------------------------------|-----------------------------|
| create         | Create new location                  | code uniqueness, required fields |
| update         | Update name, code, type, address     | code uniqueness on change   |
| activate       | Mark location as active              | location existence          |
| deactivate     | Mark location as inactive            | location existence          |

---

## 7. Domain Model (Changes)

### Location

```java
public class Location extends AggregateRoot<Id> {
    private String code;       // unique
    private String name;       // required
    private Id sellerId;       // owning seller
    private LocationType type; // WAREHOUSE or STORE
    private Address address;   // embedded value object
    private boolean active;    // status flag
}
```

### LocationType (enum)

```java
public enum LocationType {
    WAREHOUSE,
    STORE
}
```

### Address (value object)

```java
public record Address(
    String line1,
    String line2,
    String city,
    String state,
    String postalCode,
    String country   // required
) {}
```

---

## 8. Creation vs Reconstitution

### Creation
- Method: `Location.create(...)` or factory methods `createWarehouse(...)` / `createStore(...)`
- Validations:
    - Code is not blank
    - Name is not blank
    - SellerId is not null
    - Type is not null
    - Address country is not blank

### Reconstitution
- Constructor: `new Location(id, sellerId, code, name, type, address, active)`
- Assumptions:
    - Persisted data is already valid

---

## 9. Persistence & Mapping Strategy

### Repository interface
- `LocationRepository` (domain): `findById`, `findByCode`, `save`, `delete`, `existsByCode`
- `LocationQueryRepository` (read): `queryAll`, `queryByActive`, `queryByType`

### Assembler
- `LocationJpaAssembler` (interface) → `LocationJpaAssemblerImpl`
- `LocationEntityMapper`: maps domain fields to JPA entity
- `LocationMapper`: maps JPA entity back to domain

### Schema

```sql
CREATE TABLE location (
    id          BIGSERIAL PRIMARY KEY,
    uuid        VARCHAR(36)  NOT NULL UNIQUE,
    code        VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    seller_id   VARCHAR(36)  NOT NULL,
    type        VARCHAR(20)  NOT NULL,
    street      VARCHAR(255),
    street2     VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(100),
    postal_code VARCHAR(20),
    country     VARCHAR(100),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_location_code ON location(code);
CREATE INDEX idx_location_type ON location(type);
```

---

## 10. Application Services

### Commands

| Command                           | Handler                          | Description                        |
|-----------------------------------|----------------------------------|------------------------------------|
| `CreateLocationCommand`           | `CreateLocationCommandHandler`   | Creates a new location             |
| `UpdateLocationCommand`           | `UpdateLocationCommandHandler`   | Updates location fields            |
| `ActivateLocationCommand`         | `ActivateLocationCommandHandler` | Activates a location               |
| `DeactivateLocationCommand`       | `DeactivateLocationCommandHandler`| Deactivates a location             |

### Queries

| Query                           | Handler                          | Description                        |
|---------------------------------|----------------------------------|------------------------------------|
| `GetLocationQuery`              | `GetLocationQueryHandler`        | Get location by ID                 |
| `GetLocationByCodeQuery`        | `GetLocationByCodeQueryHandler`  | Get location by unique code        |
| `ListLocationsQuery`            | `ListLocationsQueryHandler`      | List locations with pagination     |

---

## 11. API Endpoints

| Method | Endpoint                              | Auth Header       | Request Body                   | Response                          |
|--------|---------------------------------------|-------------------|--------------------------------|-----------------------------------|
| POST   | `/api/v1/locations`                   | `X-Actor-Id`      | `CreateLocationRequest`        | `201` `EntityModel<LocationResponse>` |
| PATCH  | `/api/v1/locations/{locationId}`      | `X-Actor-Id` (opt)| `UpdateLocationRequest`        | `200` `EntityModel<LocationResponse>` |
| POST   | `/api/v1/locations/{locationId}/activate`   | `X-Actor-Id` (opt)| —                              | `200` `EntityModel<LocationResponse>` |
| POST   | `/api/v1/locations/{locationId}/deactivate` | `X-Actor-Id` (opt)| —                              | `200` `EntityModel<LocationResponse>` |
| GET    | `/api/v1/locations/{locationId}`       | —                 | —                              | `200` `EntityModel<LocationResponse>` |
| GET    | `/api/v1/locations/code/{code}`        | —                 | —                              | `200` `EntityModel<LocationResponse>` |
| GET    | `/api/v1/locations`                    | —                 | Query params                   | `200` `PagedModel<EntityModel<LocationResponse>>` |

### Query parameters for list endpoint

| Parameter   | Type          | Required | Description                        |
|-------------|---------------|----------|------------------------------------|
| `active`    | Boolean       | No       | Filter by active/inactive status   |
| `type`      | LocationType  | No       | Filter by location type            |
| `page`      | int           | No       | Page index (default 0)             |
| `size`      | int           | No       | Page size (default 20)             |

### Request/Response schemas

**CreateLocationRequest:**
```json
{
  "code": "WH-001",
  "name": "Main Warehouse",
  "type": "WAREHOUSE",
  "address": {
    "line1": "123 Industrial Blvd",
    "line2": "Suite 100",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "US"
  }
}
```

**UpdateLocationRequest:**
```json
{
  "code": "WH-001-UPDATED",
  "name": "Main Warehouse East",
  "type": "WAREHOUSE",
  "address": {
    "line1": "456 Industrial Blvd",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62702",
    "country": "US"
  }
}
```

**LocationResponse:**
```json
{
  "id": "loc-uuid-1",
  "code": "WH-001",
  "name": "Main Warehouse",
  "type": "WAREHOUSE",
  "active": true,
  "address": {
    "line1": "123 Industrial Blvd",
    "line2": "Suite 100",
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "US"
  }
}
```

---

## 12. Integration & Side Effects

### Domain Events

| Event                     | When Published                        | Consumers                        |
|---------------------------|---------------------------------------|----------------------------------|
| `LocationCreatedEvent`    | Location is created                   | Audit log, notification          |
| `LocationUpdatedEvent`    | Location is updated                   | Audit log                        |
| `LocationActivatedEvent`  | Location is activated                 | Audit log, inventory allocation  |
| `LocationDeactivatedEvent`| Location is deactivated               | Audit log, inventory blocking    |

### Cross-module effects
- Deactivating a location should prevent new inventory assignments and stock operations against that location
- Inventory allocation must consider only active locations

---

## 13. Error Scenarios

| Scenario                         | HTTP Status | Error Detail                           |
|----------------------------------|-------------|----------------------------------------|
| Code already exists              | 409         | `Location code already in use`         |
| Location not found               | 404         | `Location not found`                   |
| Missing required fields          | 400         | Validation errors (code, name, type)   |
| Invalid location type            | 400         | `Invalid location type`                |
| Missing `X-Actor-Id` on create   | 400         | `Actor ID is required`                 |
| Country not provided in address  | 400         | `Address country is required`          |

---

## 14. Implementation Order

1. Location domain aggregate and Address value object
2. LocationType enum
3. LocationRepository interface and LocationQueryRepository interface
4. LocationJpaAssembler, LocationEntityMapper, LocationMapper
5. LocationJpaRepository and DefaultLocationRepository
6. CQRS commands: Create, Update, Activate, Deactivate
7. CQRS queries: GetById, GetByCode, List
8. Request/Response DTOs and mappers
9. LocationCommandService and LocationQueryService
10. LocationController (REST endpoints)
11. LocationModelAssembler (HATEOAS)
12. Tests: domain unit, repository integration, service unit, controller integration

---

## 15. Definition of Done

- [X] All invariants enforced in domain
- [X] Repository implementation complete with event publishing
- [X] All REST endpoints implemented and documented
- [X] Request validation in place
- [X] Error scenarios handled with appropriate HTTP status codes
- [X] Domain events published on lifecycle changes
- [X] Tests cover domain, repository, application services, and API endpoints
