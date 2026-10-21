# Tech Specification: Storefront Query & Product Discovery

> **Summary:** Projects denormalized, channel-scoped product discovery documents asynchronously from write-domain events to deliver sub-50ms storefront browsing and PDP lookups.
> **Classification:** Architectural Pattern / CQRS Read Model / Platform Infrastructure
> **Supporting Modules:** `discovery-application` / `discovery-persistence-adapter` / `store/storefrontquery`
> **Related Architecture ADRs:** [ADR-001 System Architecture](../../dev/system/ADR_001-System_architecture.md), [ADR-002 Module-Scoped Outbox](../../dev/system/ADR_002-Module_scoped_outbox_architecture.md)

---

## 1. Why We Need It

### The Problem

High-traffic storefront product listing and PDP queries cannot execute live distributed joins across four transactional write domains (Catalog, Inventory, Pricing, Sales Channel). Direct multi-module queries create severe latency amplification, cascading failures, and database connection exhaustion under burst customer traffic.

### Technology Decision & Alternatives

| Technology Option | Decision | Evaluation Rationale |
| :--- | :--- | :--- |
| **Denormalized Projection Store** | **Adopted** | Delivers sub-50ms customer reads with zero runtime coupling to write databases and bounded contexts. |
| Live Fan-Out / Join-on-Read | Rejected | Introduces cascading failure modes, unbounded read latency, and high read lock contention on write aggregates. |
| External Search Engine (Elasticsearch) | Deferred | High operational complexity for initial deployment scale; can be introduced later by swapping the persistence adapter. |
| Direct DB Views across Module Schemas | Rejected | Violates modular monolith encapsulation boundaries and prevents future independent database extraction. |

### Key Benefits & Trade-offs

**Core Benefits:**
- Delivers predictable low-latency customer browsing decoupled from write database workload.
- Customer browse remains fully operational even during transient outages of upstream pricing or inventory write services.
- Clean separation between seller catalog authoring APIs and customer discovery models.

**Known Trade-offs:**
- Eventual consistency creates a brief window where projected display price or inventory status lags behind write transactions.
- Denormalizes product titles, prices, and stock counts into dedicated projection tables.
- Requires projector idempotency, event timestamp ordering checks, and a background rebuild mechanism.

---

## 2. What It Is

### Core Concept

Storefront Query decouples customer read traffic from transactional write aggregates through an asynchronous event-driven projector. Domain integration events update denormalized channel-scoped documents, which are queried via dedicated read use cases.

```mermaid
flowchart LR
    subgraph WriteDomains ["Transactional Write Domains"]
        Source["Domain Event Producers\n(Catalog, Inventory, Pricing, Channel)"]
    end

    subgraph StorefrontQueryEngine ["Storefront Query Engine"]
        Listener["Projection Listener"]
        Projector["Document Projector"]
        Store[("Discovery Document Store")]
        QueryService["Storefront Query Service"]
    end

    subgraph CustomerSurface ["Customer Surface"]
        Customer["Customer Browser / PDP"]
    end

    Source -->|"1. Outbox events"| Listener
    Listener -->|"2. Dispatch command"| Projector
    Projector -->|"3. Atomic upsert"| Store
    Customer -->|"4. Browse / PDP query"| QueryService
    QueryService -->|"5. Filtered view read"| Store
```

### Internal Mechanics & Lifecycle

A discovery product projection transitions through availability states depending on catalog publication, parent product status, sales channel enablement, and inventory availability.

```mermaid
stateDiagram-v2
    [*] --> Unprojected
    Unprojected --> VisibleBuyable: Variant Published & Channel Enabled & Stock Available & Quoted
    VisibleBuyable --> OutOfStock: Stock Depleted (Stock = 0 & Tracked)
    OutOfStock --> VisibleBuyable: Stock Restocked (Stock > 0)
    VisibleBuyable --> Hidden: Channel Disabled or Product Draft/Archived
    OutOfStock --> Hidden: Channel Disabled or Product Draft/Archived
    Hidden --> VisibleBuyable: Channel Enabled & Product Active
    VisibleBuyable --> Unpublished: All Variants Unpublished or Product Deleted
    OutOfStock --> Unpublished: All Variants Unpublished or Product Deleted
    Hidden --> Unpublished: All Variants Unpublished or Product Deleted
    Unpublished --> [*]
```

---

## 3. How We Use It in This System

### Project Architecture Stacking

The projection pipeline adheres to hexagonal layering. Inbound events trigger command handlers in `store`, which delegate to application use cases in `discovery-application` backed by `discovery-persistence-adapter`.

```mermaid
flowchart TD
    subgraph StorefrontQueryWeb ["store / storefrontquery"]
        REST["StorefrontProductController\n(Tier 2 API Root)"]
        EventListener["BuyableOfferProjectionEventListener\n(@EventListener)"]
        CmdHandler["ProjectDiscoveryProductCommandHandler\n(@StorefrontQueryTransactional)"]
        QryHandler["SearchBuyableOffersQueryHandler\n(@StorefrontQueryReadTransactional)"]
    end

    subgraph DiscoveryApp ["discovery-application"]
        ProjectUC["ProjectDiscoveryProductUseCase"]
        SearchUC["SearchBuyableOffersUseCase"]
        Policy["DiscoveryOfferPolicy"]
        ViewPort["DiscoveryProductQueryPort (Outbound)"]
        RepoPort["DiscoveryProductRepository (Outbound)"]
    end

    subgraph DiscoveryPersist ["discovery-persistence-adapter"]
        RepoAdapter["DiscoveryProductRepositoryAdapter"]
        QueryAdapter["DiscoveryProductQueryAdapter"]
        JPA["DiscoveryProductJpaRepository"]
        Table[("discovery_products Table")]
    end

    REST --> QryHandler
    QryHandler --> SearchUC
    EventListener --> CmdHandler
    CmdHandler --> ProjectUC
    ProjectUC --> Policy
    ProjectUC --> RepoPort
    SearchUC --> ViewPort
    RepoPort --> RepoAdapter
    ViewPort --> QueryAdapter
    RepoAdapter --> JPA
    QueryAdapter --> JPA
    JPA --> Table
```

### Runtime Flow: Event-Driven Projection Refresh

When upstream domains publish integration events, the projection listener receives the event and executes an idempotent refresh.

```mermaid
sequenceDiagram
    autonumber
    participant EventBus as Spring EventPublisher
    participant Listener as Projection Listener
    participant Projector as Project Discovery Product Service
    participant Ports as Catalog / Pricing / Inventory Ports
    participant Policy as Discovery Offer Policy
    participant Store as Discovery Product Repository

    EventBus->>Listener: Integration Event (occurredAt)
    Listener->>Projector: execute(ProjectDiscoveryProductCommand)
    Projector->>Store: findBySalesChannelAndProduct(channelId, productId)
    Store-->>Projector: existing projection (updatedAt)
    alt occurredAt < updatedAt
        Projector-->>Listener: Ignore stale event (no-op)
    else occurredAt >= updatedAt
        Projector->>Ports: Query variant details, price quote, available stock
        Ports-->>Projector: variant, price, inventory availability
        Projector->>Policy: Evaluate buyability and product visibility
        Policy-->>Projector: buyable=true/false, visible=true/false
        Projector->>Store: save(DiscoveryProductProjection)
    end
```

### Runtime Flow: Customer Storefront Query

Customer browse and PDP requests query the pre-aggregated projection directly without consulting write modules.

```mermaid
sequenceDiagram
    autonumber
    participant Client as Storefront Client
    participant Controller as StorefrontProductController
    participant QueryService as Storefront Product Query Service
    participant ChannelPort as Sales Channel Lookup Port
    participant QueryAdapter as Discovery Product Query Adapter

    Client->>Controller: GET /api/v1/storefront-query/products?salesChannelId={id}
    Controller->>QueryService: list(salesChannelId, pageable)
    QueryService->>ChannelPort: isEnabled(salesChannelId)
    alt Channel disabled or missing
        QueryService-->>Controller: ChannelDisabled / SalesChannelRequired
        Controller-->>Client: 400 Bad Request
    else Channel enabled
        QueryService->>QueryAdapter: searchVisible(salesChannelId, pageable)
        QueryAdapter-->>QueryService: Page<DiscoveryProductView>
        QueryService-->>Controller: Page<BuyableOfferResponse>
        Controller-->>Client: 200 OK (PagedModel + HAL links)
    end
```

---

## 4. Specification & Contracts

### Normative Requirements

#### 1. Projection Idempotency & Stale Event Protection
- The projection service MUST compare incoming event `occurredAt` timestamps against existing projection `updatedAt` timestamps.
- If `occurredAt` is strictly before `updatedAt`, the service MUST discard the update without writing to the database.
- Projection saves MUST persist `occurredAt` as the document's new `updatedAt` to preserve ordering across event redeliveries.

#### 2. Strict Channel Scoping
- Single-channel events (`Published`, `Unpublished`) MUST project only for the targeted channel ID.
- Multi-channel variant and product events MUST resolve distinct product-channel pairs before recalculating.
- Handlers MUST deduplicate SKU matches and product IDs to ensure linear $O(M)$ projection complexity.

#### 3. Buyability & Visibility Evaluation
- An offer MUST be marked `buyable = true` if and only if:
  1. The variant has an active publication to the target sales channel.
  2. The parent product status equals `"ACTIVE"`.
  3. The sales channel status equals `"ENABLED"`.
  4. Pricing quotes a valid price matching the channel's required currency code.
  5. Available routed inventory is greater than 0, or inventory tracking is disabled (`untracked = true`).
- A product document MUST be marked `visible = true` if and only if the sales channel is enabled, parent product status is `"ACTIVE"`, and at least one variant is published to the channel.

#### 4. Fail-Closed Customer Queries
- Direct queries against missing or blank `salesChannelId` MUST throw `StorefrontQueryServiceError.SalesChannelRequired` (HTTP 400).
- Queries targeting a disabled sales channel MUST fail closed with `StorefrontQueryServiceError.ChannelDisabled` (HTTP 400).
- PDP lookups for unprojected, deleted, or invisible products MUST throw `StorefrontQueryServiceError.OfferNotFound` (HTTP 404).

#### 5. Projection Rebuild Capability
- The system MUST provide an asynchronous rebuild command (`RebuildDiscoveryProductsUseCase`) that reads all publications from Catalog and executes full recalculations.
- Rebuild operations MUST be safe to run concurrently with live traffic without causing deadlocks or corrupting valid projection states.

### HTTP & HATEOAS Endpoints

| Endpoint | Method | Response Model | Description |
| :--- | :--- | :--- | :--- |
| `/api/v1/storefront-query` | `GET` | `RepresentationModel<?>` | Tier 2 API Root exposing discovery links. |
| `/api/v1/storefront-query/products` | `GET` | `PagedModel<EntityModel<BuyableOfferResponse>>` | Paged collection of visible products for channel. |
| `/api/v1/storefront-query/products/{slug}` | `GET` | `EntityModel<BuyableOfferResponse>` | Product detail representation with variant offers. |

### Configuration Knobs

| Property / Parameter | Logical Name | Default | Description |
| :--- | :--- | :--- | :--- |
| `fallbackCurrency` | Fallback Channel Currency | `"MMK"` | Default currency code applied when channel currency is unspecified. |
| `defaultPageSize` | Default Browse Page Size | `20` | Default number of product cards returned per page. |
| `maxPageSize` | Maximum Browse Page Size | `100` | Upper limit for client-specified pagination requests. |
