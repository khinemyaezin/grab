# R12. Outbound Infrastructure & Platform Adapters

Load when working with platform kits, storage adapters, transactional outbox, or workflow infrastructure.

## Scope & Components

Outbound platform adapters provide technical capabilities driven by the application:

| Module | Location | Responsibility |
|---|---|---|
| Storage Adapter | `storage-adapter-s3/` | Implements `FileStoragePort` using AWS S3 / MinIO. |
| Outbox Infrastructure | `outbox-infrastructure/` | Transactional outbox event publishing, persistence, and polling/processing. |
| Workflow Infrastructure | `workflow-infrastructure/` | Saga orchestrations, step execution, compensating transactions. |
| Logger Adapter | `logger-slf4j/` | Structured logging implementation. |

## Rules

### 1. Storage (`storage-adapter-s3`)
- Implements `FileStoragePort` declared in `framework`.
- Does not contain business logic. Handles S3/MinIO upload, download, presigned URLs, and bucket management.
- Wired into `store` via Spring `@Configuration`.

### 2. Outbox (`outbox-infrastructure`)
- Outbox table lives in each bounded context's datasource.
- Events are written inside the business transaction.
- Outbox poller/processor publishes events asynchronously to consumers.

### 3. Workflow (`workflow-infrastructure`)
- Three-layer platform kit: `framework.workflow` -> `workflow-infrastructure` -> `store/workflows`.
- No separate `workflow-domain` or `workflow-application`.
- Orchestrates multi-context seller/store flows without two-phase commit.

## MUST
- Implement ports defined in `framework` or application layer.
- Keep platform adapters decoupled from specific domain models.
- Use module-specific datasources and transaction managers where persistence is needed.

## MUST NOT
- Bypass ports to invoke infrastructure directly from domain aggregates or policies.
- Introduce direct dependencies between domain/application modules and specific cloud providers.
