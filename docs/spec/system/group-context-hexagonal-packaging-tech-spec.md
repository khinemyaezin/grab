# Tech Spec: Domain Group Context Hexagonal Packaging

- **Summary:** Standardizes the common hexagonal package topology, inbound/outbound ports, adapters, and CQRS boundaries across all e-commerce domain group services.
- **Type:** Architecture Pattern
- **Feature Spec:** [Modular Hexagonal Package Structure](../../features/feat_009-modular-hexagonal-package-structure.md)

---

## 1. Problem & Decision

### What breaks without this?

Without a uniform package blueprint for domain groups, individual bounded contexts construct inconsistent folder hierarchies, directly expose internal handlers to outer callers, and leak infrastructure types across module borders. This creates circular dependencies, breaks compile-time encapsulation, and prevents independently deploying or extracting domain services into microservices.

### Decision

| Option | Decision | Why |
|:-------|:---------|:----|
| **Standardized Hexagonal Group Packaging** | Adopted | Enforces a strict separation of public ingress contracts (`port/`) from encapsulated domain implementations, adapters, and CQRS handlers (`internal/`). |
| Ad-Hoc Per-Module Package Layouts | Rejected | Leads to structural drift, unpredictable dependency flows, and frequent violations of module isolation boundaries. |
| Traditional Controller-Service-DAO Stacks | Rejected | Bleeds database annotations and presentation concerns directly into business domain aggregates. |

### Trade-offs

| Benefits | Trade-offs |
|:---------|:-----------|
| Uniform structure guarantees zero framework leaks and makes cross-context onboarding seamless. | Requires disciplined package conventions and additional adapter boilerplate. |
| Enables seamless microservice extraction by maintaining strictly isolated ports and adapters. | Deeper directory nesting for simple query or command operations. |

---

## 2. How It Works

### Core Concept

Each domain group service (`com.grab.app.{group}`) organizes its bounded contexts into public inbound ports and internal implementations. External callers interact exclusively with public port interfaces (`message` and `event`), while internal inbound adapters translate requests into commands or queries routed through isolated per-service buses.


### How It Works Step by Step

| Step | What Happens | Why |
|:-----|:-------------|:----|
| 1 | Inbound request arrives at the public port interface (`port.message`). | Decouples external callers from internal implementation details. |
| 2 | Inbound adapter (`internal.adapter.inbound.message`) translates payload into an immutable command or query record. | Enforces strict contract boundaries and isolates transport types. |
| 3 | Inbound adapter dispatches record to the domain group's isolated `CommandBus` or `QueryBus`. | Preserves strict CQRS separation and prevents cross-service bus leaking. |
| 4 | Targeted CQRS handler (`internal.command` or `internal.query`) invokes domain logic and mutates aggregate state. | Business invariants remain owned by pure domain entities. |
| 5 | Aggregate mutations and integration events are written via outbound adapters (`persistence` and `event`). | Ensures atomic persistence and at-least-once outbox delivery. |

---

## 3. How We Use It

### Where It Fits in Our System / Architecture & Layer Stacking

### Detailed Package Structure

```text
com.grab.app.{group}
  ├── internal/
  │   └── config/                                   # Group-wide shared database pool & CQRS bus config
  └── {domain}/
      ├── port/                                     # Public Inbound Ports (Public API)
      │   ├── message/                              # Inbound capability & use case interfaces
      │   └── event/                                # Intra-integration event listener interfaces
      └── internal/                                 # Encapsulated implementation details
          ├── config/                               # Domain-specific Spring configuration
          ├── command/                              # CQRS Command Handlers & write models
          ├── query/                                # CQRS Query Handlers & read projections
          └── adapter/
              ├── inbound/                          # Inbound Port Implementations (Ingress)
              │   ├── message/                      # Implements port.message & publishes to bus
              │   └── event/                        # Implements port.event integration listener
              └── outbound/                         # Outbound Port Implementations (Egress)
                  ├── event/                        # Integration event publisher (Transactional Outbox)
                  └── message/                      # Intra-domain query adapter importing other ports
```

### Component Responsibilities

| Component | Role | What It Does |
|:----------|:-----|:-------------|
| `com.grab.app.{group}.internal.config` | Group Infrastructure Config | Initializes shared connection pools, Flyway streams, and isolated bus instances for the group. |
| `{domain}.port.message` | Public Inbound Port | Exposes use cases and query capabilities to other modules and platform BFFs. |
| `{domain}.port.event` | Public Event Port | Declares typed listener interfaces for consuming intra-domain integration events. |
| `{domain}.internal.adapter.inbound` | Inbound Adapter | Implements public ports, maps external payloads, and publishes to the local bus. |
| `{domain}.internal.adapter.outbound` | Outbound Adapter | Persists aggregate state to JPA repositories and emits outbox events to downstream consumers. |
| `{domain}.internal.command` | Write Pipeline | Enforces transactional consistency and executes domain logic on aggregate roots. |
| `{domain}.internal.query` | Read Pipeline | Evaluates read queries and returns projection views without touching write repositories. |

---

## 4. Implementation Rules

> These are **non-negotiable contracts**. An implementation is compliant only when **all** rules are satisfied.

| Rule | Requirement |
|:-----|:------------|
| **R-01** | Bounded contexts **MUST** expose public interfaces solely via `{group}.{domain}.port.message` and `{group}.{domain}.port.event`. |
| **R-02** | All implementations, adapters, command/query handlers, and JPA entities **MUST** reside strictly within `{group}.{domain}.internal` packages. |
| **R-03** | External modules **MUST NOT** import or access classes within any `{domain}.internal` package directly. |
| **R-04** | Inbound adapters **MUST** translate incoming requests into immutable command/query records and dispatch exclusively via the local `CommandBus` or `QueryBus`. |
| **R-05** | Cross-domain synchronous queries **MUST** be implemented in `adapter.outbound.message` by importing the public query port of the target domain. |
| **R-06** | Outbound persistence adapters **MUST** remain intra-domain and **MUST NOT** access database tables or repositories outside their owned bounded context. |
| **R-07** | Group configuration in `{group}.internal.config` **MUST** register isolated `CommandBus` and `QueryBus` beans scoped exclusively to the service group. |

