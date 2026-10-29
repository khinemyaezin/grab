# Tech Spec: Seller Platform BFF Hexagonal Packaging

- **Summary:** Defines the stateless hexagonal package structure, REST ingress adapters, platform CQRS buses, and outbound domain capability adapters for the Seller Central Platform BFF (`com.grab.app.platform.seller`).
- **Type:** Architecture Pattern
- **Feature Spec:** [Modular Hexagonal Package Structure](../../features/feat_009-modular-hexagonal-package-structure.md)

---

## 1. Problem & Decision

### What breaks without this?

Seller Central orchestrates merchant back-office workflows, catalog management, inventory restocking, and order fulfillment. Without a dedicated stateless BFF package structure, web controllers perform direct SQL queries, duplicate domain logic across endpoints, and leak presentation-specific JSON formats and HATEOAS link logic into core domain services. This creates tight presentation coupling and prevents independently deploying or scaling seller-facing interfaces.

### Decision

| Option | Decision | Why |
|:-------|:---------|:----|
| **Stateless Hexagonal Platform BFF (`com.grab.app.platform.seller`)** | Adopted | Provides tailored seller APIs, REST ingress pipelines, and HATEOAS representation while delegating all business mutations to domain group capability ports with zero local persistence. |
| Direct Controller Ingress inside Domain Groups | Rejected | Pollutes pure domain and application layers with HTTP serialization, Spring MVC controllers, and persona-specific API view requirements. |
| Monolithic Shared Web Layer | Rejected | Tangles seller back-office administrative APIs with high-throughput shopper storefront endpoints, causing security and scalability conflicts. |

### Trade-offs

| Benefits | Trade-offs |
|:---------|:-----------|
| Zero persistent state eliminates database connection pool overhead in the platform layer. | Additional indirection layer mapping REST DTOs to outbound domain capability calls. |
| Seller APIs can be refactored, versioned, or scaled independently without touching domain aggregates. | Requires maintaining dedicated outbound message adapters for each domain service group. |

---

## 2. How It Works

### Core Concept

The Seller Platform BFF (`com.grab.app.platform.seller`) serves as an ingress gateway tailored specifically to merchants and administrators. It follows hexagonal architecture with public REST contracts in `port/rest`, an internal ingress pipeline in `internal/adapter/inbound/rest`, an isolated platform CQRS bus, and outbound message adapters that invoke domain group ports (`catalog`, `order`, `account`, `channel`). It owns zero database schemas or JPA repositories.

### How It Works Step by Step

| Step | What Happens | Why |
|:-----|:-------------|:----|
| 1 | Seller client sends HTTP request to REST controller (`com.grab.app.platform.seller.internal.adapter.inbound.rest.controller`). | Ingress point parses HTTP headers, validates request payloads, and extracts authentication context. |
| 2 | Controller delegates to `CommandService` (for writes) or `QueryService` (for reads). | Enforces strict CQRS separation at the HTTP boundary. |
| 3 | Service dispatches immutable command/query record to the isolated Platform CQRS bus. | Decouples HTTP request lifecycle from business orchestration. |
| 4 | Platform handler executes and delegates to outbound message adapter (`adapter.outbound.message`). | Invokes target domain group via its public `port.message` interface. |
| 5 | Outbound adapter returns immutable result record; controller transforms into `EntityModel<T>` with HATEOAS links. | Delivers rich, hypermedia-driven REST responses without leaking domain entities. |

---

## 3. How We Use It

### Where It Fits in Our System / Architecture & Layer Stacking

### Detailed Package Structure

```text
com.grab.app.platform.seller
  ├── port/
  │   └── rest/                                     # Public REST Controller Interfaces & Contracts
  └── internal/
      ├── config/                                   # Platform CQRS Bus & Security Configuration
      ├── adapter/
      │   ├── inbound/
      │   │   └── rest/                             # Web Adapter Pipeline
      │   │       ├── controller/                   # Spring MVC REST Controllers
      │   │       ├── assembler/                    # HATEOAS Representation Model Assemblers
      │   │       ├── dto/                          # Request & Response DTO Records
      │   │       ├── mapper/                       # MapStruct Mappers (DTO <-> Command/Query)
      │   │       └── service/                      # Inbound CQRS Ingress Services
      │   └── outbound/
      │       └── message/                          # Domain Group Capability Adapters
      ├── command/                                  # Platform Command Handlers
      └── query/                                    # Platform Query Handlers
```

### Component Responsibilities

| Component | Role | What It Does |
|:----------|:-----|:-------------|
| `port.rest` | Ingress Interface | Defines Spring REST endpoint contracts and OpenAPI annotations. |
| `adapter.inbound.rest.controller` | HTTP Boundary | Parses HTTP requests, validates DTOs, and invokes `CommandService`/`QueryService`. |
| `adapter.inbound.rest.service` | CQRS Dispatcher | Thin services that dispatch commands or queries to the platform bus without inline logic. |
| `internal.config` | BFF Config | Configures isolated platform `CommandBus`/`QueryBus` beans and web security filters. |
| `internal.command` / `query` | BFF Orchestrator | Coordinates multi-domain calls for seller journeys without persisting local state. |
| `adapter.outbound.message` | Domain Invoker | Implements outbound ports that invoke `com.grab.app.{group}.{domain}.port.message`. |

---

## 4. Implementation Rules

> These are **non-negotiable contracts**. An implementation is compliant only when **all** rules are satisfied.

| Rule | Requirement |
|:-----|:------------|
| **R-01** | The Seller Platform BFF **MUST** reside within `com.grab.app.platform.seller`. |
| **R-02** | The module **MUST NOT** contain any persistence packages (`adapter.persistence`), JPA entities, Spring Data repositories, or direct database connections. |
| **R-03** | REST controllers **MUST NOT** inject domain repositories, buses, or use cases directly; they **MUST** delegate exclusively to `CommandService`, `QueryService`, and `ModelAssembler`. |
| **R-04** | All cross-domain calls to backend domain groups **MUST** pass through `adapter.outbound.message` importing public domain capability ports (`port.message`). |
| **R-05** | Database transactions (`@Transactional`) **MUST NOT** be demarcated on Platform BFF controllers, services, or adapters. |
| **R-06** | Controller responses **MUST** return `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>` with valid kebab-case HATEOAS links. |

