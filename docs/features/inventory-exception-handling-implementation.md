# Feature: Inventory Exception Handling Framework Implementation

## Overview

This feature defines how to implement and maintain the exception-handling architecture described in:

- [ADR-009: MessageSource-Based Exception Handling Framework](../architecture/decisions/ADR-009-exception-handling-framework.md)

The design is code-first and contract-stable:

- typed errors at domain/service/infra layers
- machine-readable error codes (`inv.*`, `shr.*`)
- centralized API mapping to RFC7807-style `ProblemDetail`

## Goals

- Standardize error modeling across framework, inventory domain, infrastructure, and store.
- Eliminate ad-hoc `IllegalArgumentException` usage in business flows.
- Guarantee predictable HTTP status and payload shape for clients.
- Keep exception class count small while preserving typed error contracts.

## Scope

In scope:

- `framework` exception contracts and CQRS misconfiguration errors
- `inventory-domain` typed validation/allocation errors
- `inventory-infrastructure` persistence error translation
- `store` inventory service errors and global API exception mapping
- test coverage for domain + infra + API error contracts

Out of scope:

- catalog module full migration (follow-up iteration)
- API version bump (`/api/v1` remains unchanged)
- localization strategy redesign beyond existing `MessageResolver`

## Target Design

Each module follows this structure:

1. Sealed `*Error` interface implementing `MessageSource`
2. Typed nested error records with required arguments
3. Single wrapper `*Exception extends DomainException`
4. API layer maps by `ErrorCategory` and `code`

Current module implementations:

- Framework: `FrameworkError`, `FrameworkException`
- Inventory domain: `InventoryDomainError`, `InventoryDomainValidationException`
- Inventory infrastructure: `InventoryInfraError`, `InventoryInfraException`
- Store service layer: `InventoryServiceError`, `InventoryServiceException`
- Shared API layer: `SharedError`, `SharedException`, `GlobalApiExceptionHandler`

## Error Code Contract

Code prefixes:

- `inv.domain.*` - inventory domain invariants
- `inv.alloc.*` - allocation failures
- `inv.service.*` - application/service use-case failures
- `inv.infra.*` - infrastructure/persistence failures
- `shr.*` - shared request/internal failures

HTTP status mapping:

| ErrorCategory | HTTP |
|---|---|
| `BAD_REQUEST` | `400` |
| `NOT_FOUND` | `404` |
| `CONFLICT` | `409` |
| `BUSINESS_RULE` | `422` |
| `INTERNAL` | `500` |

RFC7807 payload extensions:

- `code`
- `args`
- `traceId`
- `path`
- `timestamp`
- `module`
- `retryable`
- `retryAfterMs` (only for selected retryable cases)

## Detailed Implementation

### 1) Framework foundation

Implementation points:

- `DomainException` exposes `getMessageSource()`
- `FrameworkError` provides typed internal framework errors
- `FrameworkException` wraps framework-level typed errors
- command/query bus throw `FrameworkException` for missing handler registration

Files:

- `framework/src/main/java/com/grab/framework/exception/DomainException.java`
- `framework/src/main/java/com/grab/framework/exception/FrameworkError.java`
- `framework/src/main/java/com/grab/framework/exception/FrameworkException.java`
- `framework/src/main/java/com/grab/framework/cqrs/command/impl/DefaultCommandBus.java`
- `framework/src/main/java/com/grab/framework/cqrs/query/impl/DefaultQueryBus.java`

### 2) Inventory domain normalization

Implementation points:

- domain validation and allocation failures modeled in `InventoryDomainError`
- domain invariants throw `InventoryDomainValidationException`
- code namespace normalized to `inv.domain.*` and `inv.alloc.*`
- remove legacy/unused domain error enums

Files:

- `inventory-domain/src/main/java/com/inventory/domain/exception/InventoryDomainError.java`
- `inventory-domain/src/main/java/com/inventory/domain/exception/InventoryDomainValidationException.java`
- `inventory-domain/src/main/java/com/inventory/domain/aggregate/InventoryItem.java`
- `inventory-domain/src/main/java/com/inventory/domain/valueobject/InventoryQuantity.java`
- `inventory-domain/src/main/java/com/inventory/domain/valueobject/ReorderConfig.java`

### 3) Inventory infrastructure translation

Implementation points:

- repositories translate Spring `DataAccessException` into typed infra errors
- conflict path: `DataIntegrityViolationException -> inv.infra.persistence.conflict`
- internal path: other data access failures -> `inv.infra.persistence.internal`
- translation wrapper centralized via `InventoryPersistenceExecutor`

Files:

- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/exception/InventoryInfraError.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/exception/InventoryInfraException.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/support/InventoryPersistenceExecutor.java`
- `inventory-infrastructure/src/main/java/com/inventory/infrastructure/repository/jpa/impl/*.java`

### 4) Store inventory service layer

Implementation points:

- handlers and queries throw direct typed service exceptions:
  - `new InventoryServiceException(new InventoryServiceError.X(...))`
- static helper/factory coupling removed from handlers/queries
- `InventoryServiceException` derives stable default message by error type

Files:

- `store/src/main/java/com/grab/store/inventory/internal/exception/InventoryServiceError.java`
- `store/src/main/java/com/grab/store/inventory/internal/exception/InventoryServiceException.java`
- `store/src/main/java/com/grab/store/inventory/internal/command/handler/*.java`
- `store/src/main/java/com/grab/store/inventory/internal/query/handler/*.java`

### 5) Shared API mapping and message resolution

Implementation points:

- `GlobalApiExceptionHandler` maps `DomainException` and shared request errors
- malformed JSON and bean validation mapped to `shr.request.*`
- unexpected failures mapped to `shr.internal.unexpected`
- module inferred by code prefix (`inv.`, `cat.`, fallback `shared`)

Files:

- `store/src/main/java/com/grab/store/shared/exception/GlobalApiExceptionHandler.java`
- `store/src/main/java/com/grab/store/shared/exception/SharedError.java`
- `store/src/main/java/com/grab/store/shared/exception/SharedException.java`
- `store/src/main/java/com/grab/store/shared/exception/SharedErrors.java`
- `store/src/main/java/com/grab/store/shared/exception/SpringMessageResolver.java`
- `store/src/main/resources/messages.properties`

## Testing Strategy

### Domain

- contract tests for code/category/args per typed error
- rule tests verify domain throws typed wrapper exceptions

### Infrastructure

- repository translation tests for conflict/internal persistence failures

### Store/API

- command/query tests assert typed service exceptions
- integration tests assert:
  - HTTP status mapping
  - `ProblemDetail.code`
  - `args` payload
  - `retryable` and `retryAfterMs` behavior

Primary commands:

- `./mvnw -pl framework test`
- `./mvnw -pl inventory-domain test`
- `./mvnw -pl inventory-infrastructure test`
- `./mvnw -pl store -am test`

## Rollout and Compatibility

- Treat `ProblemDetail.code` as public contract for clients.
- For inventory/shared migration, immediate switch to `inv.*` / `shr.*` is allowed.
- Any client depending on legacy `exception.*` / `error.*` must be updated in same release window.

## Acceptance Criteria

- No raw `IllegalArgumentException` in inventory domain rules.
- No raw `IllegalArgumentException` in store inventory handlers/queries.
- Infrastructure persistence failures translated to typed `inv.infra.*` errors.
- API responses consistently return RFC7807 with `code` and `args`.
- Retry metadata present only for configured retryable scenarios.
- Automated tests pass for framework, inventory-domain, inventory-infrastructure, and store.

