# Tech Spec: Marketplace Platform BFF Hexagonal Packaging

- **Summary:** Defines the stateless hexagonal package structure, storefront REST ingress pipelines, platform CQRS buses, and outbound domain capability adapters for the Marketplace Shopper Platform BFF (`com.grab.app.platform.marketplace`).
- **Type:** Architecture Pattern
- **Feature Spec:** [Modular Hexagonal Package Structure](../../features/feat_009-modular-hexagonal-package-structure.md)

---

## 1. Problem & Decision

### What breaks without this?

The Marketplace Platform BFF powers consumer storefront browsing, contextual regional product discovery, cart manipulation, and checkout initiation. Without an autonomous stateless BFF package structure, high-volume consumer traffic queries transactional write databases directly, storefront controllers perform cross-schema joins across product and cart tables, and shopper session context entangles with core order domain aggregates. This causes database connection pool exhaustion, degrades search latency, and creates massive security blast radiuses across the entire platform.

### Decision

| Option | Decision | Why |
|:-------|:---------|:----|
| **Stateless Hexagonal Platform BFF (`com.grab.app.platform.marketplace`)** | Adopted | Provides high-throughput, shopper-facing storefront APIs, HATEOAS navigability, and edge shopping context aggregation while delegating all reads to discovery and writes to domain capability ports with zero local persistence. |
| Direct Ingress to Domain Contexts | Rejected | Forces domain service groups to handle client HTTP routing, authentication tokens, rate limiting, and consumer-tailored JSON formatting. |
| Shared Monolithic API Gateway | Rejected | Blurs customer browsing needs with merchant back-office administration, preventing distinct caching, autoscaling, and traffic-shaping policies. |

### Trade-offs

| Benefits | Trade-offs |
|:---------|:-----------|
| Read-heavy storefront traffic is cleanly isolated from write-heavy merchant administration. | Requires maintaining dedicated outbound message adapters for each consumed domain capability. |
| Storefront controllers can be horizontally scaled and cached at the edge without database connection limits. | Multi-domain aggregation (e.g., product details plus cart badge) requires explicit adapter orchestration. |

---

## 2. How It Works

### Core Concept

The Marketplace Platform BFF (`com.grab.app.platform.marketplace`) is a stateless consumer gateway. It exposes public REST contracts under `port/rest`, executes an ingress pipeline through `internal/adapter/inbound/rest`, dispatches records across an isolated Platform CQRS bus, and delegates downstream interactions to outbound adapters in `internal/adapter/outbound/message` that invoke public domain ports (`discovery`, `cart`, `order`, `customer`). It maintains zero database persistence.

### How It Works Step by Step

| Step | What Happens | Why |
|:-----|:-------------|:----|
| 1 | Shopper issues HTTP request to storefront REST controller (`com.grab.app.platform.marketplace.internal.adapter.inbound.rest.controller`). | Ingress point handles HTTP request routing, header parsing, and input validation. |
| 2 | Controller delegates to `CommandService` (for mutations like add-to-cart) or `QueryService` (for search/browse). | Enforces strict CQRS separation at the web entry layer. |
| 3 | Service dispatches immutable command/query record to the isolated Platform CQRS bus. | Decouples web transport handling from application orchestration. |
| 4 | Platform handler calls outbound message adapter (`adapter.outbound.message`). | Maps BFF requirements to domain capability contracts (`discovery.port.message`, `cart.port.message`). |
| 5 | Outbound adapter returns immutable result record; controller transforms into `EntityModel<T>` with HATEOAS links. | Delivers sanitized, hypermedia-navigable responses to shoppers without exposing internal domain types. |

---

## 3. How We Use It

### Where It Fits in Our System / Architecture & Layer Stacking

### Detailed Package Structure

```text
com.grab.app.platform.marketplace
  ├── port/
  │   └── rest/                                     # Public Storefront REST Controller Interfaces
  └── internal/
      ├── config/                                   # Storefront CQRS Bus & Rate Limiting Configuration
      ├── adapter/
      │   ├── inbound/
      │   │   └── rest/                             # Web Ingress Pipeline
      │   │       ├── controller/                   # Spring MVC REST Controllers
      │   │       ├── assembler/                    # HATEOAS Model Assemblers
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
| `port.rest` | Storefront Ingress Interface | Exposes public REST endpoints and OpenAPI documentation for consumers. |
| `adapter.inbound.rest.controller` | HTTP Boundary | Validates request DTOs, extracts shopping session tokens, and delegates to services. |
| `adapter.inbound.rest.service` | CQRS Dispatcher | Thin services that dispatch commands or queries to the storefront platform bus. |
| `internal.config` | BFF Config | Configures isolated platform `CommandBus`/`QueryBus` beans and public security policies. |
| `internal.command` / `query` | BFF Orchestrator | Coordinates consumer workflows across cart, checkout, and discovery without local state. |
| `adapter.outbound.message` | Domain Invoker | Implements outbound ports that invoke `com.grab.app.{group}.{domain}.port.message`. |

---

## 4. Implementation Rules

> These are **non-negotiable contracts**. An implementation is compliant only when **all** rules are satisfied.

| Rule | Requirement |
|:-----|:------------|
| **R-01** | The Marketplace Platform BFF **MUST** reside within `com.grab.app.platform.marketplace`. |
| **R-02** | The module **MUST NOT** contain any persistence packages (`adapter.persistence`), JPA entities, Spring Data repositories, or direct database connections. |
| **R-03** | Product catalog searches and browse requests **MUST** invoke `discovery.port.message` via outbound message adapters; direct queries to catalog tables are **PROHIBITED**. |
| **R-04** | REST controllers **MUST NOT** inject domain repositories, buses, or use cases directly; they **MUST** delegate exclusively to `CommandService`, `QueryService`, and `ModelAssembler`. |
| **R-05** | Database transactions (`@Transactional`) **MUST NOT** be demarcated on Platform BFF controllers, services, or adapters. |
| **R-06** | Controller responses **MUST** return `ResponseEntity<EntityModel<T>>` or `ResponseEntity<PagedModel<EntityModel<T>>>` with valid kebab-case HATEOAS links. |

