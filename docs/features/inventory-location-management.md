# Feature: Inventory Location Management

## Overview

Manage physical inventory topology (`Location -> Zone -> Bin`) as a complete business capability, not only as persistence data.

This feature closes the gap between:

- existing domain/infrastructure models for location hierarchy
- missing application/API workflows needed by operators

## Current State (Implemented)

- Domain model exists:
  - `Location` aggregate
  - `Zone`, `Bin` entities
  - location type and zone type enums
- Persistence exists:
  - location/zone/bin entities and repositories
  - mapping + assembler-based graph persistence

## What Is Missing

### 1. No Location API Surface

- no `/api/v1/...` endpoints for location, zone, or bin management
- no location-focused request/response DTOs

### 2. No Location Application Handlers

- no command handlers for create/update/activate/deactivate location
- no command handlers for zone/bin lifecycle
- no query handlers for location lists/details

### 3. Missing Validation at Inventory Creation Boundary

- inventory creation accepts arbitrary `locationId`
- no check that location exists
- no check that location is active

### 4. Missing Policy for Deactivation/Delete with Dependent Inventory

- no explicit rule enforcement tying location lifecycle to existing inventory records

### 5. Missing Location Events

- no location/zone/bin domain events for outbox publication and integration

### 6. Missing Test Coverage

- no store-level tests for location workflows
- no infrastructure tests focused on location repository graph synchronization

## What Is Needed

### Application Layer

- add commands:
  - `CreateLocation`
  - `UpdateLocation`
  - `ActivateLocation`
  - `DeactivateLocation`
  - `AddZone` / `UpdateZone` / `RemoveZone`
  - `AddBin` / `UpdateBin` / `RemoveBin`
- add queries:
  - `GetLocation`
  - `GetLocationByCode`
  - `ListLocations`
  - `ListActiveLocations`
  - `ListLocationsByType`

### API Layer

Suggested endpoints:

- `POST /api/v1/locations`
- `PATCH /api/v1/locations/{locationId}`
- `POST /api/v1/locations/{locationId}/activate`
- `POST /api/v1/locations/{locationId}/deactivate`
- `POST /api/v1/locations/{locationId}/zones`
- `PATCH /api/v1/locations/{locationId}/zones/{zoneId}`
- `DELETE /api/v1/locations/{locationId}/zones/{zoneId}`
- `POST /api/v1/locations/{locationId}/zones/{zoneId}/bins`
- `PATCH /api/v1/locations/{locationId}/zones/{zoneId}/bins/{binId}`
- `DELETE /api/v1/locations/{locationId}/zones/{zoneId}/bins/{binId}`
- `GET /api/v1/locations/{locationId}`
- `GET /api/v1/locations?active=true&type=WAREHOUSE`

### Validation and Rules

- enforce `locationId` existence + `active=true` before creating inventory
- enforce uniqueness:
  - location code (global)
  - zone code (within location)
  - bin code (within zone)
- block location deactivation/deletion when active inventory remains, unless explicit override policy is defined

### Events

- add and publish:
  - `LocationCreatedEvent`
  - `LocationUpdatedEvent`
  - `LocationDeactivatedEvent`
  - `ZoneChangedEvent`
  - `BinChangedEvent`

### Testing

- command handler tests (success + validation failures)
- query handler tests (filters and mapping)
- repository tests (zone/bin sync on save/update/remove)
- controller integration tests for full request/response lifecycle

## Acceptance Criteria

- [ ] Operator can create and update locations through API
- [ ] Operator can manage zones and bins through API
- [ ] Inventory creation fails for unknown/inactive `locationId`
- [ ] Location deactivation/delete follows dependency policy with inventory items
- [ ] Location lifecycle emits outbox-backed events
- [ ] Automated tests cover domain rules, persistence graph sync, and API behavior

## Out of Scope (Initial Iteration)

- geospatial search (nearest warehouse)
- slotting optimization algorithms
- external WMS synchronization orchestration
