# Tech Spec: Hexagonal Modular Service Packaging

- **Summary:** Defines the hexagonal package structure, domain service groupings, and stateless platform BFF layers for independent scalability and microservice extraction readiness.
- **Type:** Architecture Pattern
- **Feature Spec:** [Modular Hexagonal Package Structure](../../features/feat_009-modular-hexagonal-package-structure.md)

---

## 1. Problem & Decision

### What breaks without this?

Without a standardized hexagonal package structure, business rules become entangled with database frameworks, web controllers access entities directly, and cross-module dependencies form circular tangles. This prevents independent feature development, causes regressions during framework upgrades, and makes extracting services into autonomous microservices nearly impossible without an expensive rewrite.

### Decision

| Option | Decision | Why |
|:-------|:---------|:----|
| **Hexagonal Packaging with Platform BFFs** | Adopted | Strict separation of domain, application, persistence, and stateless presentation layers; enables in-process modular monolith execution today and seamless microservice extraction tomorrow. |
| Traditional 3-Tier Layered Architecture | Rejected | Allows presentation and database concerns to bleed into domain models, leading to tight coupling and leaky abstractions. |
| Immediate Physical Microservices | Deferred | High operational overhead, network latency, distributed transaction complexity, and CI/CD infrastructure cost before domain boundaries stabilize. |

### Trade-offs

| Benefits | Trade-offs |
|:---------|:-----------|
| Domain logic remains completely framework-agnostic and 100% unit-testable. | Higher initial boilerplate (explicit ports, use case interfaces, and mappers). |
| Platform BFFs tailor APIs to specific clients (seller vs shopper) without data coupling. | Requires strict discipline and automated ArchUnit rules to maintain package integrity. |
| Clear extraction boundaries allow converting any domain group into a microservice without touching domain logic. | Multiple sub-packages per domain module increase structural navigation depth. |

---

## 2. How It Works

### Core Concept

The architecture decomposes the platform into three primary tiers: **Stateless Platform BFFs** (`seller`, `marketplace`), **Domain Service Groups** (`catalog`, `order`, `account`, `channel`), and a dedicated **Cross-Domain Workflow Context Service** (`workflows`). Every domain and context service strictly applies Hexagonal Architecture (Ports and Adapters), ensuring that dependencies only point inward toward pure domain and orchestration logic.

Platform BFFs interact with domain groups for single-domain capabilities, and invoke the **Workflow Context Service** via dedicated **Workflow Capability Ports** for multi-domain business sagas.

```mermaid
flowchart TD
    subgraph PlatformBFF ["Platform BFF Layer (Stateless Assembly)"]
        Controller["REST Controller"] --> BFFService["BFF Application Service / Handler"]
        BFFService --> CapabilityPort["Domain Capability Port"]
        BFFService --> WorkflowPort["Workflow Capability Port"]
    end

    subgraph WorkflowContextService ["Workflow Context Service (Stateful Saga)"]
        WorkflowPort --> WFInboundPort["Inbound Workflow Port"]
        WFInboundPort --> WFEngine["Transactional Workflow Engine"]
        WFEngine --> StepDefs["Process & Step Definitions"]
        WFEngine --> WFStore["Workflow Store & Outbox (workflows DB)"]
    end

    subgraph DomainServiceGroup ["Domain Service Group"]
        CapabilityPort --> InboundPort["Inbound UseCase Port"]
        InboundPort --> UseCaseService["UseCase Implementation Service"]
        UseCaseService --> DomainCore["Domain Aggregates & Policies"]
        UseCaseService --> OutboundPort["Outbound Repository Port"]
        OutboundPort --> PersistenceAdapter["Persistence Adapter (JPA)"]
    end

    PersistenceAdapter --> Database[("Service Schema")]
    WFEngine -.->|"Outbox Request Events"| InboundPort
    DomainServiceGroup -.->|"Completion Signals"| WFEngine
```

### How It Works Step by Step

| Step | What Happens | Why |
|:-----|:-------------|:----|
| 1 | Inbound request arrives at Platform BFF Controller (`com.grab.app.platform.{platform}.internal.adapter.inbound.rest`). | Handles HTTP parsing, validation, and HATEOAS representation without containing business logic. |
| 2 | BFF Controller maps transport DTO to application command/query and dispatches via Platform Service. | Ensures transport contracts remain decoupled from internal domain command records. |
| 3a | For single-domain operations, Platform Service invokes Domain Capability Port (`com.grab.{platform}.application.port.outbound.{Domain}QueryPort`). | Keeps the BFF agnostic of whether the domain service is hosted in-process or over HTTP. |
| 3b | For multi-domain operations (e.g. product creation, checkout), Platform Service invokes Workflow Capability Port (`com.grab.{platform}.application.port.outbound.*WorkflowPort`). | Delegates complex distributed state coordination to the workflow context without leaking saga persistence or transactions into the BFF. |
| 4 | Workflow Context Service receives request via Inbound Port, launches the workflow process, and tracks step progression and compensations. | Manages the saga lifecycle atomically under `workflowsTransactionManager`. |
| 5 | Domain Services receive requests via Inbound Port or workflow integration events. | Directs execution to pure use case orchestration. |
| 6 | UseCase Service loads aggregate via Outbound Repository Port and applies business rules. | Business logic resides strictly in domain entities and policies. |
| 7 | Persistence Adapter (`com.{domain}.adapter.persistence`) persists state changes and local outbox records. | Infrastructure frameworks (Spring Data JPA, Hibernate) remain isolated at the boundary. |

---

## 3. How We Use It

### Package Structure Specification

The codebase is organized into bounded domain service groups, a cross-domain workflow context service, and platform BFF modules:

```
com.grab.app/
├── catalog/                                # Product Service Group
│   ├── catalog/                            # Catalog Bounded Context
│   ├── inventory/                          # Inventory Bounded Context
│   └── pricing/                            # Pricing Bounded Context
├── order/                                  # Order Service Group
│   ├── cart/                               # Cart Bounded Context
│   ├── order/                              # Order Bounded Context
│   └── payment/                            # Payment Bounded Context
├── account/                                # Account Service Group
│   ├── identity/                           # Identity Bounded Context
│   ├── merchant/                           # Merchant Bounded Context
│   └── customer/                           # Customer Bounded Context
├── channel/                                # Channel Service Group
│   ├── saleschannel/                       # Sales Channel Bounded Context
│   ├── region/                             # Region Bounded Context
│   └── discovery/                          # Discovery Projection Context
├── workflows/                              # Cross-Domain Workflow Context Service (Saga Orchestration)
└── platform/                               # Platform BFF Services (Stateless)
    ├── seller/                             # Seller Central BFF
    └── marketplace/                        # Marketplace Storefront BFF
```

#### Detailed Bounded Context Layout (`com.{domain}`)

```
com.{domain}.application/
├── port/
│   ├── inbound/                            # Inbound Use Case Ports (*UseCase.java)
│   └── outbound/                           # Outbound Ports (*Repository, *QueryPort, *EventPublisher)
├── service/                                # Use Case Implementations (*Service.java)
└── model/
    ├── read/                               # Read Projections (*View.java)
    └── write/                              # Command/Result Records

com.{domain}.adapter.persistence/
├── adapter/                                # Outbound Adapters (*RepositoryAdapter, *QueryAdapter)
├── entity/                                 # JPA Entities (*JpaEntity.java)
├── repository/jpa/                         # Spring Data JPA Repositories (*JpaRepository.java)
└── mapper/                                 # Entity/Domain Mappers (*PersistenceMapper.java)
```

#### Domain Group Internal Layout (`com.grab.app.{group}`)

```
com.grab.app.{group}/
├── port/                                   # Public Group Ports
│   ├── message/                            # Intra-Group Message Interfaces
│   └── event/                              # Intra-Group Integration Event Interfaces
└── internal/
    ├── config/                             # Shared Database & Bus Configuration
    └── {domain}/
        ├── config/                         # Domain-Specific Configuration
        ├── adapter/
        │   ├── inbound/                    # Inbound Adapters (Event/Message Listeners -> Bus)
        │   └── outbound/                   # Outbound Adapters (Event/Message Publishers)
        ├── command/                        # Command Handlers
        └── query/                          # Query Handlers
```

#### Workflow Context Service Layout (`com.grab.app.workflows`)

```
com.grab.app.workflows/
├── port/                                   # Public Inbound Capability Ports for BFFs / Clients
│   └── *WorkflowPort.java                  # e.g., SellableProductWorkflowPort.java (request/response records)
└── internal/
    ├── config/                             # Workflow DataSource, JPA, and workflowsTransactionManager
    ├── definition/                         # Saga Process & Step Definitions (pure orchestration)
    │   └── *Definition.java                # e.g., CreateSellableProductDefinition.java
    ├── adapter/
    │   ├── inbound/
    │   │   ├── event/                      # Inbound Signal Listeners (*WorkflowSignalInbox.java)
    │   │   └── rest/                       # Direct administrative/sweeper endpoints if needed
    │   ├── outbound/
    │   │   ├── outbox/                     # Emits request events to domains via Workflow Outbox
    │   │   └── persistence/                # JpaWorkflowStore integration
    │   └── api/
    │       └── adapter/                    # Provider Adapter implementing public port (*WorkflowPortAdapter.java)
    └── service/                            # Sweepers, Recovery, and Execution Services
```

#### Platform BFF Layout (`com.grab.app.platform.{platform}`)

```
com.grab.app.platform.{platform}/
├── port/
│   └── rest/                               # Public REST Controller Interfaces
└── internal/
    ├── config/                             # BFF Bus Configuration
    ├── adapter/
    │   ├── inbound/
    │   │   └── rest/                       # Controllers, DTOs, Mappers, Assemblers, Services
    │   └── outbound/
    │       ├── domain/                     # Adapters implementing Domain Group Capability Ports
    │       └── workflow/                   # Adapters implementing Workflow Capability Ports
    ├── command/                            # BFF Command Handlers
    └── query/                              # BFF Query Handlers
```

### Component Responsibilities

| Component | Role | What It Does |
|:----------|:-----|:-------------|
| `{domain}-domain` | Pure Domain Core | Contains aggregates, entities, value objects, domain policies, and outbound repository interfaces. Framework-free. |
| `{domain}-application` | Application Orchestration | Implements use cases, defines command/query records, and orchestrates aggregate mutation and query port lookups. |
| `{domain}-adapter-persistence` | Intra-Domain Persistence | Implements domain repository ports using JPA/Hibernate; handles entity-to-domain mapping and local outbox persistence. |
| `workflows` | Cross-Domain Saga Orchestrator | Manages durable multi-step workflows, step definitions, checkpoints, and compensation recovery using dedicated `workflowsTransactionManager` and workflow outbox. Exposes public workflow ports to BFFs. |
| `platform.{platform}` | Client Experience Gateway | Aggregates domain capabilities for specific client types (seller vs marketplace) without owning persistent state. Dispatches multi-domain workflows through outbound workflow ports. |
| `app` (Root Boot Module) | Application Assembler | Configures Spring Boot environment, registers beans, and wires platform BFFs, domain groups, and workflow context into the runtime process. |

---

## 4. Implementation Rules

> These are **non-negotiable contracts**. An implementation is compliant only when **all** rules are satisfied.

- **RULE-PKG-01: Framework Isolation in Domain**
  Domain modules (`{domain}-domain`) MUST NOT import or depend on Spring, Jakarta/JPA, Jackson, or MapStruct frameworks. All domain logic MUST use pure Java.

- **RULE-PKG-02: Strict Dependency Flow**
  Dependencies MUST only flow inward: `store` / `app` → `adapter.persistence` & `application` → `domain`. The domain layer MUST NEVER depend on application or persistence layers.

- **RULE-PKG-03: Stateless Platform BFF Modules**
  Platform BFF modules (`com.grab.app.platform.seller`, `com.grab.app.platform.marketplace`) MUST NOT contain any persistence packages, JPA repositories, or database entities. They are strictly stateless application gateways.

- **RULE-PKG-04: Intra-Domain Persistence Scope**
  Persistence adapters (`com.{domain}.adapter.persistence`) MUST NOT contain cross-module adapters or reference repositories/entities of another domain. All cross-module communication MUST occur through published capability ports or integration events.

- **RULE-PKG-05: Modulith Package Encapsulation**
  Classes residing within `internal/` packages MUST NOT be imported or accessed by external modules. Public interfaces MUST reside in root `port/` packages or published named interfaces.

- **RULE-PKG-06: Workflow Context Isolation & BFF Port Invocations**
  Cross-domain workflows MUST reside in the dedicated `workflows` context service (`com.grab.app.workflows`), backed by `workflowsTransactionManager` and its dedicated workflow outbox. Platform BFFs MUST NOT contain workflow engines, step listeners, saga definitions, or persistent workflow state. BFFs MUST interact with workflows exclusively through consumer-owned outbound capability ports delegating to provider-published workflow ports.
