# Inventory Error Audit and Organization Plan

## Status
Proposed (March 9, 2026)

## Scope
This plan covers error handling for:

- `inventory-domain`
- `inventory-infrastructure`
- `store` (inventory module)

## 1) Current-State Audit

### A. `inventory-domain`

Current strengths:

- Typed sealed errors exist:
  - `InventoryDomainError` with 6 typed records
  - `InventoryDomainValidationException` wrapper
- Allocation service returns typed `MessageSource` errors in `AllocationResult.failure(...)`.

Current gaps:

- `IllegalArgumentException` still used in domain rules: **17 occurrences**
  - `InventoryQuantity`
  - `ReorderConfig`
  - `InventoryItem.validateReceiveType(...)`
  - `InventoryItem.validatePositiveQuantity(...)`
- Error code namespaces are mixed:
  - `error.allocation.*`
  - `exception.inventory.*`
- `InventoryItemError` enum exists but is empty/unused.

### B. `inventory-infrastructure`

Current state:

- `inventory-infrastructure/.../exception` package is empty.
- Repository adapters do not translate persistence failures into typed infrastructure errors.
- `jpaRepository.save(...)` and query operations currently let persistence/runtime exceptions propagate.

Impact:

- No stable infrastructure error code contract (`inv.infra.*`).
- API layer cannot reliably classify infra failures (conflict vs internal vs not found).

### C. `store` (inventory)

Current state:

- `store/.../inventory/internal/exception` package is empty.
- Command/query handlers throw `IllegalArgumentException` directly: **41 occurrences**
  - `orElseThrow(() -> new IllegalArgumentException(...))`: **24**
  - direct `throw new IllegalArgumentException(...)`: **17**
- Inventory tests assert `IllegalArgumentException`: **11 test assertions**.
- No global API exception handler exists (`@RestControllerAdvice` absent).
- No RFC7807 `ProblemDetail` mapping is currently active.

Impact:

- Unstable and inconsistent error response contract.
- HTTP status mapping is implicit/accidental.
- Hard to localize/standardize error messages.

### D. Framework constraints affecting all modules

- `DomainException` carries `MessageSource`, but has no public accessor for transport mapping.
- `MessageSource` already provides required contract fields (`kind`, `code`, `args`) and should remain the core abstraction.

## 2) Target Error Organization

Organize by layer and prefix:

- Domain rules: `inv.domain.*` and `inv.alloc.*`
- Application/service use-cases (store handlers): `inv.service.*`
- Infrastructure/persistence: `inv.infra.*`
- Shared request/system: `shr.*`

Type model per layer:

1. Sealed `*Error` interface implementing `MessageSource`
2. One wrapper `*Exception` class extending `DomainException`
3. API mapper translates by `ErrorCategory` + `code`

Status mapping:

- `BUSINESS_RULE -> 422`
- `BAD_REQUEST -> 400`
- `NOT_FOUND -> 404`
- `CONFLICT -> 409`
- `INTERNAL -> 500`

## 3) Implementation Plan

### Phase 0: Foundation (framework + API edge)

Goals:

- Enable centralized translation to HTTP without changing all call sites first.

Changes:

1. Add `getMessageSource()` to `DomainException`.
2. Add shared `@RestControllerAdvice` in `store/shared/exception`.
3. Implement RFC7807 response shape with `code`, `args`, `path`, `timestamp`, `traceId`.
4. Add message bundle files for code-to-text resolution.

### Phase 1: Domain normalization (`inventory-domain`)

Goals:

- Remove raw `IllegalArgumentException` from domain invariants.
- Keep all domain errors typed and machine-readable.

Changes:

1. Expand `InventoryDomainError` with missing validation records.
2. Replace all 17 domain `IllegalArgumentException` throws with:
   - `new InventoryDomainValidationException(new InventoryDomainError.<Type>(...), "...")`
3. Normalize code prefixes:
   - `error.allocation.* -> inv.alloc.*`
   - `exception.inventory.* -> inv.domain.*`
4. Remove or repurpose empty `InventoryItemError` enum.

### Phase 2: Infrastructure translation (`inventory-infrastructure`)

Goals:

- Prevent raw persistence exceptions from leaking upward.

Changes:

1. Create `InventoryInfrastructureError` sealed interface (`MessageSource`).
2. Create `InventoryInfrastructureException extends DomainException`.
3. In default repositories, translate persistence exceptions:
   - uniqueness/constraint violations -> `CONFLICT` (`inv.infra.persistence.conflict`)
   - missing data where required -> `NOT_FOUND` (`inv.infra.persistence.not_found`)
   - unexpected data access failures -> `INTERNAL` (`inv.infra.persistence.internal`)
4. Add infrastructure tests for translation behavior.

### Phase 3: Store/service layer cleanup (`store/inventory`)

Goals:

- Remove handler-level `IllegalArgumentException`.
- Expose stable service/use-case error codes.

Changes:

1. Create `InventoryServiceError` sealed interface (`MessageSource`).
2. Create `InventoryServiceException extends DomainException`.
3. Replace all 41 handler `IllegalArgumentException` usages with typed service exceptions.
4. Standardize common service error codes:
   - location/inventory/reservation/zone/bin not found
   - duplicate code conflicts
   - invalid ownership/mismatch
   - inactive location constraints
5. Update affected tests (11 assertions) from `IllegalArgumentException` to typed exceptions/codes.

### Phase 4: API contract hardening

Goals:

- Make error responses stable and testable for clients.

Changes:

1. Add integration tests for inventory/location error scenarios (HTTP + code + args).
2. Ensure validation errors map to `shr.request.*`.
3. Ensure domain/service/infra errors map to `inv.*`.
4. Re-enable and extend currently disabled location controller integration test coverage.

## 4) File Targets

Framework:

- `framework/src/main/java/com/grab/framework/exception/DomainException.java`

Inventory domain:

- `inventory-domain/src/main/java/com/inventory/domain/exception/InventoryDomainError.java`
- `inventory-domain/src/main/java/com/inventory/domain/exception/InventoryDomainValidationException.java`
- `inventory-domain/src/main/java/com/inventory/domain/aggregate/InventoryItem.java`
- `inventory-domain/src/main/java/com/inventory/domain/valueobject/InventoryQuantity.java`
- `inventory-domain/src/main/java/com/inventory/domain/valueobject/ReorderConfig.java`

Inventory infrastructure:

- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/exception/*`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/impl/*.java`

Store (inventory + shared):

- `store/src/main/java/com/grab/store/inventory/internal/exception/*`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/*.java`
- `store/src/main/java/com/grab/store/inventory/internal/query/handler/*.java`
- `store/src/main/java/com/grab/store/shared/exception/*`
- `store/src/main/resources/messages*.properties`

Tests:

- `inventory-domain/src/test/java/**`
- `inventory-infrastructure/src/test/java/**`
- `store/src/test/java/com/grab/store/inventory/**`
- `store/src/test/java/com/grab/store/shared/**`

## 5) Acceptance Criteria

- No `IllegalArgumentException` remains in `inventory-domain` business rules.
- No `IllegalArgumentException` remains in store inventory command/query handlers.
- Inventory infrastructure translates persistence failures into typed `inv.infra.*` errors.
- API responses return RFC7807 payload with stable `code` and `args`.
- Inventory errors use consistent prefix strategy (`inv.*`), shared errors use `shr.*`.
- Tests validate code/category/status contracts end-to-end.
