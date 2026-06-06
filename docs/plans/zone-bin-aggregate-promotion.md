# Plan: Promote Zone and Bin to Independent Aggregate Roots

## Overview

This plan refactors the inventory module to promote `Zone` and `Bin` from child entities of the `Location` aggregate to independent aggregate roots. This follows DDD best practices for:
- **Reduced lock contention**: Each aggregate can be updated independently
- **Targeted sharding**: Aggregates can be distributed across database partitions
- **Independent scalability**: Each aggregate type can scale based on its own load patterns
- **Bounded context clarity**: Each aggregate has a single, focused responsibility

## Design Principles

### Rule 1: Reference by Identity Only
- `Zone` holds `locationId` (UUID string), not a `Location` object reference
- `Bin` holds `zoneId` (UUID string), not a `Zone` object reference

### Rule 2: Eventual Consistency for Lifecycle Changes
- When `Location` is deactivated → publishes `LocationDeactivatedEvent`
- Event listener catches event → dispatches commands to deactivate all zones for that location
- When `Zone` is deactivated → publishes `ZoneDeactivatedEvent`
- Event listener catches event → dispatches commands to deactivate all bins for that zone

---

## Impact Analysis

### 1. Domain Layer Changes

#### 1.1 Location Aggregate (Simplified)

**Current State:**
- Owns `List<Zone>` as child entities
- Has methods: `addZone()`, `removeZone()`, `updateZone()`, `addBinToZone()`, `removeBinFromZone()`, `updateBinInZone()`
- Raises `ZoneChangedEvent` and `BinChangedEvent`
- `deactivate()` cascades to all zones

**Target State:**
- **Remove** all zone/bin management methods
- **Remove** `List<Zone>` field
- **Keep** only location-level concerns: code, name, type, address, active status
- **Keep** location events: `LocationCreatedEvent`, `LocationUpdatedEvent`, `LocationActivatedEvent`, `LocationDeactivatedEvent`
- `deactivate()` only deactivates the location itself (no cascade)

**Files to Modify:**
- `inventory-domain/src/main/java/com/inventory/domain/aggregate/Location.java`

#### 1.2 Zone Aggregate (New Aggregate Root)

**Current State:**
- `Zone extends Entity<Id>` (child entity)
- Owns `List<Bin>` as child entities
- No `locationId` field (implicitly owned by Location)

**Target State:**
- `Zone extends AggregateRoot<Id>` (aggregate root)
- **Add** `locationId` field (Id type, references Location by identity)
- **Remove** `List<Bin>` (bins become independent aggregates)
- **Add** zone-level methods: `update()`, `activate()`, `deactivate()`
- **Add** zone events: `ZoneCreatedEvent`, `ZoneUpdatedEvent`, `ZoneActivatedEvent`, `ZoneDeactivatedEvent`
- `deactivate()` only deactivates the zone itself (no cascade to bins)

**Files to Modify:**
- `inventory-domain/src/main/java/com/inventory/domain/entity/Zone.java` → move to `aggregate/Zone.java`

#### 1.3 Bin Aggregate (New Aggregate Root)

**Current State:**
- `Bin extends Entity<Id>` (child entity)
- No `zoneId` field (implicitly owned by Zone)

**Target State:**
- `Bin extends AggregateRoot<Id>` (aggregate root)
- **Add** `zoneId` field (Id type, references Zone by identity)
- **Add** bin-level methods: `update()`, `activate()`, `deactivate()`
- **Add** bin events: `BinCreatedEvent`, `BinUpdatedEvent`, `BinActivatedEvent`, `BinDeactivatedEvent`

**Files to Modify:**
- `inventory-domain/src/main/java/com/inventory/domain/entity/Bin.java` → move to `aggregate/Bin.java`

#### 1.4 New Repository Interfaces

**Create:**
- `inventory-domain/src/main/java/com/inventory/domain/repository/ZoneRepository.java`
  ```java
  public interface ZoneRepository {
      Optional<Zone> findById(Id id);
      Optional<Zone> findByCodeAndLocationId(String code, Id locationId);
      List<Zone> findByLocationId(Id locationId);
      List<Zone> findByLocationIdAndActive(Id locationId, boolean active);
      Zone save(Zone zone);
      void delete(Id id);
      boolean existsByCodeAndLocationId(String code, Id locationId);
  }
  ```

- `inventory-domain/src/main/java/com/inventory/domain/repository/BinRepository.java`
  ```java
  public interface BinRepository {
      Optional<Bin> findById(Id id);
      Optional<Bin> findByCodeAndZoneId(String code, Id zoneId);
      List<Bin> findByZoneId(Id zoneId);
      List<Bin> findByZoneIdAndActive(Id zoneId, boolean active);
      Bin save(Bin bin);
      void delete(Id id);
      boolean existsByCodeAndZoneId(String code, Id zoneId);
  }
  ```

#### 1.5 New Domain Events

**Create:**
- `ZoneCreatedEvent(Id zoneId, Id locationId, String code, String name, ZoneType type, LocalDateTime occurredAt)`
- `ZoneUpdatedEvent(Id zoneId, Id locationId, String code, String name, ZoneType type, LocalDateTime occurredAt)`
- `ZoneActivatedEvent(Id zoneId, Id locationId, LocalDateTime occurredAt)`
- `ZoneDeactivatedEvent(Id zoneId, Id locationId, LocalDateTime occurredAt)`
- `BinCreatedEvent(Id binId, Id zoneId, String code, String name, Integer maxCapacity, LocalDateTime occurredAt)`
- `BinUpdatedEvent(Id binId, Id zoneId, String code, String name, Integer maxCapacity, LocalDateTime occurredAt)`
- `BinActivatedEvent(Id binId, Id zoneId, LocalDateTime occurredAt)`
- `BinDeactivatedEvent(Id binId, Id zoneId, LocalDateTime occurredAt)`

**Remove or Deprecate:**
- `ZoneChangedEvent` (replaced by specific events)
- `BinChangedEvent` (replaced by specific events)

#### 1.6 Event Listeners for Cascading Deactivation

**Create:**
- `LocationDeactivationEventListener` (in store module)
  - Listens for `LocationDeactivatedEvent`
  - Queries all zones for the location
  - Dispatches `DeactivateZoneCommand` for each zone

- `ZoneDeactivationEventListener` (in store module)
  - Listens for `ZoneDeactivatedEvent`
  - Queries all bins for the zone
  - Dispatches `DeactivateBinCommand` for each bin

**Note:** These listeners use the outbox pattern for reliability. Events are persisted atomically with the aggregate state change, then processed asynchronously.

---

### 2. Infrastructure Layer Changes

#### 2.1 JPA Entities

**LocationEntity:**
- **Remove** `@OneToMany zones` field
- **Remove** `addZone()`, `removeZone()` methods
- **Keep** all other fields (id, uuid, code, name, type, address fields, active)

**ZoneEntity:**
- **Remove** `@ManyToOne location` field
- **Remove** `@OneToMany bins` field
- **Remove** `addBin()`, `removeBin()` methods
- **Add** `locationId` column (String, not null, indexed)
- **Keep** all other fields (id, uuid, code, name, type, active)

**BinEntity:**
- **Remove** `@ManyToOne zone` field
- **Add** `zoneId` column (String, not null, indexed)
- **Keep** all other fields (id, uuid, code, name, maxCapacity, active)

**Files to Modify:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/entity/LocationEntity.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/entity/ZoneEntity.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/entity/BinEntity.java`

#### 2.2 JPA Repositories

**LocationJpaRepository:**
- **Remove** all `JOIN FETCH` queries (no longer needed)
- **Simplify** to basic CRUD operations

**ZoneJpaRepository:**
- **Update** queries to use `locationId` string column instead of `@ManyToOne`
- **Add** new queries:
  ```java
  List<ZoneEntity> findAllByLocationId(String locationId);
  List<ZoneEntity> findAllByLocationIdAndActive(String locationId, boolean active);
  Optional<ZoneEntity> findByCodeAndLocationId(String code, String locationId);
  boolean existsByCodeAndLocationId(String code, String locationId);
  ```

**BinJpaRepository:**
- **Update** queries to use `zoneId` string column instead of `@ManyToOne`
- **Add** new queries:
  ```java
  List<BinEntity> findAllByZoneId(String zoneId);
  List<BinEntity> findAllByZoneIdAndActive(String zoneId, boolean active);
  Optional<BinEntity> findByCodeAndZoneId(String code, String zoneId);
  boolean existsByCodeAndZoneId(String code, String zoneId);
  ```

**Files to Modify:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/LocationJpaRepository.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/ZoneJpaRepository.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/BinJpaRepository.java`

#### 2.3 Repository Implementations

**DefaultLocationRepository:**
- **Simplify** `save()` method (no zone graph to persist)
- **Simplify** `findById()`, `findAll()`, etc. (no graph traversal)

**Create DefaultZoneRepository:**
- Implement `ZoneRepository` interface
- Use `ZoneJpaRepository` and `ZoneJpaAssembler`
- Publish events via `DomainEventProducer`

**Create DefaultBinRepository:**
- Implement `BinRepository` interface
- Use `BinJpaRepository` and `BinJpaAssembler`
- Publish events via `DomainEventProducer`

**Files to Create:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/impl/DefaultZoneRepository.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/impl/DefaultBinRepository.java`

**Files to Modify:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/impl/DefaultLocationRepository.java`

#### 2.4 Assemblers

**LocationJpaAssembler:**
- **Simplify** to only handle Location entity (no zone/bin graph)
- **Remove** `buildFullEntityGraph()` and `toFullDomainGraph()` complexity

**Create ZoneJpaAssembler:**
- `ZoneEntity toEntity(Zone zone)`
- `Zone toDomain(ZoneEntity entity)`

**Create BinJpaAssembler:**
- `BinEntity toEntity(Bin bin)`
- `Bin toDomain(BinEntity entity)`

**Files to Create:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/ZoneJpaAssembler.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/impl/ZoneJpaAssemblerImpl.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/BinJpaAssembler.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/impl/BinJpaAssemblerImpl.java`

**Files to Modify:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/LocationJpaAssembler.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/impl/LocationJpaAssemblerImpl.java`

#### 2.5 MapStruct Mappers

**Update:**
- `ZoneEntityMapper`: Add mapping for `locationId` field
- `BinEntityMapper`: Add mapping for `zoneId` field
- `ZoneMapper`: Add mapping for `locationId` field
- `BinMapper`: Add mapping for `zoneId` field

**Files to Modify:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/ZoneEntityMapper.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/BinEntityMapper.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/ZoneMapper.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/mapper/jpa/BinMapper.java`

#### 2.6 Configuration

**InventoryInfraConfig:**
- **Add** beans for `ZoneRepository` and `BinRepository`
- **Add** beans for `ZoneJpaAssembler` and `BinJpaAssembler`
- **Update** `LocationRepository` bean (remove zone/bin assembler dependencies)

**Files to Modify:**
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/config/InventoryInfraConfig.java`

---

### 3. Store Layer Changes

#### 3.1 Command Handlers

**Remove from LocationCommandHandler:**
- All zone/bin command handlers (they currently modify Location aggregate)

**Create New Command Handlers:**
- `CreateZoneCommandHandler` - creates Zone aggregate with `locationId`
- `UpdateZoneCommandHandler` - updates Zone aggregate
- `ActivateZoneCommandHandler` - activates Zone aggregate
- `DeactivateZoneCommandHandler` - deactivates Zone aggregate
- `CreateBinCommandHandler` - creates Bin aggregate with `zoneId`
- `UpdateBinCommandHandler` - updates Bin aggregate
- `ActivateBinCommandHandler` - activates Bin aggregate
- `DeactivateBinCommandHandler` - deactivates Bin aggregate

**Files to Create:**
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/CreateZoneCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/UpdateZoneCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/ActivateZoneCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/DeactivateZoneCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/CreateBinCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/UpdateBinCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/ActivateBinCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/DeactivateBinCommandHandler.java`

**Files to Delete:**
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/AddZoneCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/UpdateZoneCommandHandler.java` (old version)
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/RemoveZoneCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/AddBinCommandHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/UpdateBinCommandHandler.java` (old version)
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/RemoveBinCommandHandler.java`

#### 3.2 Query Handlers

**Create New Query Handlers:**
- `GetZoneQueryHandler` - gets Zone by ID
- `ListZonesByLocationQueryHandler` - lists zones for a location
- `GetBinQueryHandler` - gets Bin by ID
- `ListBinsByZoneQueryHandler` - lists bins for a zone

**Files to Create:**
- `store/src/main/java/com/grab/store/inventory/internal/query/handler/GetZoneQueryHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/query/handler/ListZonesByLocationQueryHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/query/handler/GetBinQueryHandler.java`
- `store/src/main/java/com/grab/store/inventory/internal/query/handler/ListBinsByZoneQueryHandler.java`

#### 3.3 Commands and Queries

**Create New Commands:**
- `CreateZoneCommand(Id locationId, String code, String name, ZoneType type, String actorId)`
- `UpdateZoneCommand(Id zoneId, String code, String name, ZoneType type, Boolean active, String actorId)`
- `ActivateZoneCommand(Id zoneId, String actorId)`
- `DeactivateZoneCommand(Id zoneId, String actorId)`
- `CreateBinCommand(Id zoneId, String code, String name, Integer maxCapacity, String actorId)`
- `UpdateBinCommand(Id binId, String code, String name, Integer maxCapacity, Boolean active, String actorId)`
- `ActivateBinCommand(Id binId, String actorId)`
- `DeactivateBinCommand(Id binId, String actorId)`

**Create New Queries:**
- `GetZoneQuery(Id zoneId)`
- `ListZonesByLocationQuery(Id locationId, Boolean active)`
- `GetBinQuery(Id binId)`
- `ListBinsByZoneQuery(Id zoneId, Boolean active)`

**Create New Results:**
- `ZoneResult(Id id, Id locationId, String code, String name, String type, boolean active)`
- `BinResult(Id id, Id zoneId, String code, String name, Integer maxCapacity, boolean active)`

**Files to Create:**
- Multiple files in `store/src/main/java/com/grab/store/inventory/internal/command/`
- Multiple files in `store/src/main/java/com/grab/store/inventory/internal/query/`

#### 3.4 Controllers

**Option A: Separate Controllers (Recommended)**

Create:
- `ZoneController` at `/api/v1/zones`
  - `POST /` - create zone
  - `PATCH /{zoneId}` - update zone
  - `POST /{zoneId}/activate` - activate zone
  - `POST /{zoneId}/deactivate` - deactivate zone
  - `GET /{zoneId}` - get zone
  - `GET /?locationId={locationId}` - list zones by location

- `BinController` at `/api/v1/bins`
  - `POST /` - create bin
  - `PATCH /{binId}` - update bin
  - `POST /{binId}/activate` - activate bin
  - `POST /{binId}/deactivate` - deactivate bin
  - `GET /{binId}` - get bin
  - `GET /?zoneId={zoneId}` - list bins by zone

**Option B: Keep Nested URLs (Alternative)**

Keep `LocationController` but route zone/bin operations to separate services:
- `POST /api/v1/locations/{locationId}/zones` → `ZoneFacadeService.createZone()`
- `PATCH /api/v1/locations/{locationId}/zones/{zoneId}` → `ZoneFacadeService.updateZone()`
- etc.

**Recommendation:** Option A is cleaner and follows the new aggregate boundaries. The nested URL structure implies ownership which no longer exists.

**Files to Create:**
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/controller/ZoneController.java`
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/controller/BinController.java`

**Files to Modify:**
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/controller/LocationController.java` (remove zone/bin endpoints)

#### 3.5 Facade Services

**Create:**
- `ZoneFacadeService` - orchestrates zone commands and queries
- `BinFacadeService` - orchestrates bin commands and queries
- `ZoneCommandService` - dispatches zone commands
- `ZoneQueryService` - dispatches zone queries
- `BinCommandService` - dispatches bin commands
- `BinQueryService` - dispatches bin queries

**Files to Create:**
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/ZoneFacadeService.java`
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/ZoneCommandService.java`
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/ZoneQueryService.java`
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/BinFacadeService.java`
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/BinCommandService.java`
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/BinQueryService.java`

**Files to Modify:**
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/LocationFacadeService.java` (remove zone/bin methods)
- `store/src/main/java/com/grab/store/inventory/internal/api/rest/service/LocationCommandService.java` (remove zone/bin methods)

#### 3.6 DTOs and Mappers

**Create Request DTOs:**
- `CreateZoneRequest(String code, String name, String type, Id locationId)`
- `UpdateZoneRequest(String code, String name, String type, Boolean active)`
- `CreateBinRequest(String code, String name, Integer maxCapacity, Id zoneId)`
- `UpdateBinRequest(String code, String name, Integer maxCapacity, Boolean active)`

**Create Response DTOs:**
- `ZoneResponse(Id id, Id locationId, String code, String name, String type, boolean active)`
- `ZonesResponse(List<ZoneResponse> zones)`
- `BinResponse(Id id, Id zoneId, String code, String name, Integer maxCapacity, boolean active)`
- `BinsResponse(List<BinResponse> bins)`

**Create MapStruct Mappers:**
- `ZoneCommandDtoMapper`
- `ZoneQueryDtoMapper`
- `BinCommandDtoMapper`
- `BinQueryDtoMapper`

**Create HATEOAS Assemblers:**
- `ZoneModelAssembler`
- `ZonesModelAssembler`
- `BinModelAssembler`
- `BinsModelAssembler`

**Files to Create:**
- Multiple files in `store/src/main/java/com/grab/store/inventory/internal/api/rest/dto/request/`
- Multiple files in `store/src/main/java/com/grab/store/inventory/internal/api/rest/dto/response/`
- Multiple files in `store/src/main/java/com/grab/store/inventory/internal/api/rest/mapper/`
- Multiple files in `store/src/main/java/com/grab/store/inventory/internal/api/rest/assembler/`

#### 3.7 Event Listeners

**Create:**
- `LocationDeactivationEventListener`
  - `@EventListener` or `@TransactionalEventListener`
  - Listens for `LocationDeactivatedEvent`
  - Queries `ZoneRepository.findByLocationId()`
  - Dispatches `DeactivateZoneCommand` for each zone

- `ZoneDeactivationEventListener`
  - `@EventListener` or `@TransactionalEventListener`
  - Listens for `ZoneDeactivatedEvent`
  - Queries `BinRepository.findByZoneId()`
  - Dispatches `DeactivateBinCommand` for each bin

**Files to Create:**
- `store/src/main/java/com/grab/store/inventory/internal/event/LocationDeactivationEventListener.java`
- `store/src/main/java/com/grab/store/inventory/internal/event/ZoneDeactivationEventListener.java`

#### 3.8 LocationResultMapper

**Update:**
- Remove zone/bin mapping methods
- Location responses no longer include zones/bins

**Files to Modify:**
- `store/src/main/java/com/grab/store/inventory/internal/support/LocationResultMapper.java`

#### 3.9 Error Types

**Add to InventoryServiceError:**
- `ZoneNotFound(String zoneId)`
- `BinNotFound(String binId)`
- `ZoneAlreadyExists(String code, String locationId)`
- `BinAlreadyExists(String code, String zoneId)`
- `LocationNotFoundForZone(String locationId)`
- `ZoneNotFoundForBin(String zoneId)`

**Files to Modify:**
- `store/src/main/java/com/grab/store/inventory/internal/exception/InventoryServiceError.java`

#### 3.10 i18n Messages

**Add to messages.properties:**
```properties
inv.service.zone.not_found=Zone not found: {zoneId}
inv.service.bin.not_found=Bin not found: {binId}
inv.service.zone.already_exists=Zone with code {code} already exists for location {locationId}
inv.service.bin.already_exists=Bin with code {code} already exists for zone {zoneId}
inv.service.zone.location_not_found=Location not found for zone: {locationId}
inv.service.bin.zone_not_found=Zone not found for bin: {zoneId}
```

**Files to Modify:**
- `store/src/main/resources/messages.properties`

---

### 4. Database Schema Changes

**Migration Strategy:**
- Hibernate `ddl-auto=update` will handle schema changes automatically in dev
- For production, create explicit migration scripts

**Schema Changes:**

**location table:**
- No changes (zones relationship was JPA-only, not a DB column)

**zone table:**
- **Remove** `location_id` foreign key column (was `@ManyToOne`)
- **Add** `location_id` column (VARCHAR, not null, indexed) - stores UUID string
- **Add** index on `location_id`

**bin table:**
- **Remove** `zone_id` foreign key column (was `@ManyToOne`)
- **Add** `zone_id` column (VARCHAR, not null, indexed) - stores UUID string
- **Add** index on `zone_id`

**Note:** The column names stay the same, but the semantics change from FK to plain string.

---

### 5. Test Changes

**Domain Tests:**
- Update `LocationTest` to remove zone/bin tests
- Create `ZoneTest` for zone aggregate behavior
- Create `BinTest` for bin aggregate behavior

**Infrastructure Tests:**
- Update `DefaultLocationRepositoryTest` to remove zone/bin tests
- Create `DefaultZoneRepositoryTest`
- Create `DefaultBinRepositoryTest`

**Store Tests:**
- Update `LocationControllerTest` to remove zone/bin endpoint tests
- Create `ZoneControllerTest`
- Create `BinControllerTest`
- Create event listener tests

**Files to Create:**
- Multiple test files across all modules

---

## Implementation Order

### Phase 1: Domain Layer Refactoring
1. Create new domain events for Zone and Bin
2. Refactor `Zone` to aggregate root (move to `aggregate/` package, add `locationId`, add events)
3. Refactor `Bin` to aggregate root (move to `aggregate/` package, add `zoneId`, add events)
4. Simplify `Location` aggregate (remove zone/bin methods and fields)
5. Create `ZoneRepository` and `BinRepository` interfaces

### Phase 2: Infrastructure Layer Refactoring
6. Update JPA entities (remove relationships, add ID columns)
7. Update JPA repositories (new queries for ID-based lookups)
8. Create `ZoneJpaAssembler` and `BinJpaAssembler`
9. Simplify `LocationJpaAssembler`
10. Create `DefaultZoneRepository` and `DefaultBinRepository`
11. Update `DefaultLocationRepository`
12. Update `InventoryInfraConfig` (wire new beans)

### Phase 3: Store Layer - Commands and Queries
13. Create new command records (CreateZoneCommand, etc.)
14. Create new query records (GetZoneQuery, etc.)
15. Create new result records (ZoneResult, BinResult)
16. Create new command handlers
17. Create new query handlers

### Phase 4: Store Layer - API
18. Create request/response DTOs
19. Create MapStruct mappers
20. Create HATEOAS assemblers
21. Create `ZoneController` and `BinController`
22. Create facade services (ZoneFacadeService, BinFacadeService, etc.)
23. Update `LocationController` (remove zone/bin endpoints)
24. Update `LocationFacadeService` and `LocationCommandService`

### Phase 5: Event-Driven Consistency
25. Create `LocationDeactivationEventListener`
26. Create `ZoneDeactivationEventListener`

### Phase 6: Error Handling and i18n
27. Add new error types to `InventoryServiceError`
28. Add i18n messages to `messages.properties`

### Phase 7: Testing
29. Update domain tests
30. Update infrastructure tests
31. Update store tests
32. Create new tests for Zone and Bin

### Phase 8: Documentation
33. Update architecture diagrams
34. Update PRD if needed
35. Update feature documentation

---

## Risks and Mitigations

### Risk 1: Eventual Consistency Delays
**Concern:** When a location is deactivated, zones may remain active briefly until events are processed.
**Mitigation:** 
- Use `@TransactionalEventListener(phase = BEFORE_COMMIT)` for synchronous processing within the same transaction
- Or accept eventual consistency and document the behavior

### Risk 2: API Breaking Changes
**Concern:** Clients using nested URLs (`/locations/{id}/zones`) will break.
**Mitigation:**
- Keep nested URLs as aliases that redirect to new endpoints
- Or version the API and deprecate old endpoints
- Or accept breaking change if no clients exist yet

### Risk 3: Data Migration
**Concern:** Existing data may have orphaned zones/bins if location is deleted.
**Mitigation:**
- Add foreign key constraints at application level (validate location exists before creating zone)
- Or add database-level constraints if needed

### Risk 4: Performance
**Concern:** Separate aggregates may require more database queries.
**Mitigation:**
- Use caching for frequently accessed zones/bins
- Optimize queries with proper indexes
- Monitor performance and adjust as needed

---

## Success Criteria

- [ ] Location, Zone, and Bin are independent aggregate roots
- [ ] Each aggregate references others by ID only (no object references)
- [ ] Each aggregate has its own repository
- [ ] Each aggregate publishes its own domain events
- [ ] Cascading deactivation works via event listeners
- [ ] All existing functionality is preserved
- [ ] Tests pass for all three aggregates
- [ ] API endpoints work correctly (either new structure or backward-compatible)
- [ ] No N+1 query problems
- [ ] Documentation is updated

---

## Open Questions

1. **API Structure:** Should we use separate controllers (`/zones`, `/bins`) or keep nested URLs (`/locations/{id}/zones`)?
   - **Recommendation:** Separate controllers for clarity

2. **Event Processing:** Should cascading deactivation be synchronous or asynchronous?
   - **Recommendation:** Synchronous (`BEFORE_COMMIT`) for simplicity, unless performance requires async

3. **Backward Compatibility:** Do we need to support old API clients?
   - **Recommendation:** If no clients exist, accept breaking change. Otherwise, provide aliases.

4. **Delete vs Deactivate:** Should we support hard delete for zones/bins, or only soft delete (deactivation)?
   - **Recommendation:** Soft delete only, consistent with Location

5. **Validation:** Should zone/bin creation validate that the parent location/zone exists and is active?
   - **Recommendation:** Yes, validate existence but not necessarily active status (allow creating zones in inactive locations for setup purposes)
