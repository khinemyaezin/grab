# Tech Specification: Discovery Facts-Only Projection

> **Summary:** Materializes currency-agnostic catalog and inventory facts per channel/product pair; pricing is composed at request time.
> **Classification:** Architectural Pattern / CQRS Read Model
> **Supporting Modules:** `discovery-application` / `discovery-persistence-adapter` / `store/storefrontquery`
> **Related Architecture ADRs:** [SSD-001 §Phrase 4](../../system-design/001-multi_regional_contextual_discovery_system_design.md)
> **Related Feature:** [feat_006 — Discovery Wiring, Projection Reconciliation & Rebuild](../../features/feat_006-discovery-wiring-projection-reconciliation.md)

---

## 1. Why We Need It

### The Problem

Coupling prices, currency codes, compare-at amounts, and regional buyability into materialized discovery projection rows causes **combinatorial write amplification** across price updates, exchange rate shifts, and market reconfigurations. In a multi-regional commerce platform with N channels × M products × P price tiers, every price rule change forces projection rewrites across the entire combinatorial surface. Storing prices directly in search documents leads to index thrashing and stale quotes that confuse storefront consumers.

### Technology Decision & Alternatives

| Technology Option | Decision | Evaluation Rationale |
| :--- | :--- | :--- |
| **Facts-Only Projection (Prices at Request Time)** | **Adopted** | Decouples the largest write-amplification source (pricing events) from projection refresh entirely. Pricing is resolved per-request via `PricingQuoteQuery.quoteBatch` in Phase 5, using the exact regional context of the shopper. |
| Pre-Computed Price Ranges in Projection | Rejected | Requires re-projecting every affected product whenever any price rule, exchange rate, or tier changes. The combinatorial surface (channels × products × currencies × price sets) makes freshness unreliable. |
| Denormalized Price Columns per Currency | Rejected | Violates bounded context separation; ties projection schema to the Pricing module's currency model. Adding a currency or market requires a schema migration. |
| Full Search Engine (Elasticsearch/OpenSearch) | Deferred | Appropriate for future full-text search with facets. Current JPA specification-based search is sufficient for Phase 4 volume, and the facts-only model is portable to an external index. |

### Key Benefits & Trade-offs

**Core Benefits:**
- Pricing events (rule changes, exchange rate updates, market configurations) do not trigger projection writes.
- Projection refresh frequency drops by at least one order of magnitude, since catalog and inventory events are far less frequent than pricing recalculations.
- The projection schema is stable across market expansions — adding a new currency or region requires zero migration.

**Known Trade-offs:**
- Phase 4 REST responses contain no price information. Consumers must understand the phased rollout boundary.
- Request-time pricing adds latency to Phase 5 storefront responses compared to pre-materialized prices.
- Storefront search cannot filter or sort by price until an external search index is introduced.

---

## 2. What It Is

### Core Concept

The Discovery Facts-Only Projection is a CQRS read model that stores one materialized row per `(sales_channel_id, product_id)` pair. Each row captures exclusively **catalog presentation facts** (title, slug, description, thumbnail, options, product status, publication membership) and **inventory stock facts** (available quantity, stock badge, backorder, inventory management) — never currency codes, monetary amounts, or commercial buyability.

```mermaid
flowchart LR
    subgraph CorePattern ["Facts-Only CQRS Projection"]
        CatSource["Catalog Facts\n(Product, Variant, Publication)"]
        InvSource["Inventory Facts\n(Stock, Route, Backorder)"]
        SCSource["Channel Facts\n(Enabled/Disabled)"]
        Projection["Discovery Projection Row\n(Channel × Product)"]
        PriceEngine["Pricing Quote Engine\n(Phase 5 Request-Time)"]
        Response["Storefront Response\n(Facts + Quote)"]

        CatSource -->|"Presentation"| Projection
        InvSource -->|"Stock facts"| Projection
        SCSource -->|"Channel status"| Projection
        Projection -->|"Facts only"| Response
        PriceEngine -.->|"Phase 5: Compose at request time"| Response
    end
```

### Internal Mechanics & Lifecycle

> Visual lifecycle of a single projection row.

```mermaid
stateDiagram-v2
    [*] --> Absent : No product published to this channel
    Absent --> Published : First variant published to channel
    Published --> Published : Catalog/Inventory/Channel event refresh
    Published --> Tombstone : All variants unpublished or product deleted
    Tombstone --> Published : Variant republished to channel
    Tombstone --> Tombstone : Stale event refresh (watermark retained)
```

---

## 3. How We Use It in This System

### Architecture & Layer Stacking

```mermaid
flowchart TB
    subgraph StoreLayer ["Application Composition Layer (store)"]
        EventListener["Event Listener\n(Integration Events → CommandBus)"]
        CmdHandler["Command Handler\n(Retry Loop + REQUIRES_NEW)"]
    end

    subgraph AppLayer ["Application Layer (discovery-application)"]
        ProjectSvc["Projection Service\n(Reconcile & Compose)"]
        ProjectionPolicy["Projection Policy\n(Compose / Invalidate / Watermark)"]
        ProjectionModel["Projection Model\n(DiscoveryProductProjection record)"]
    end

    subgraph InfraLayer ["Infrastructure Layer (discovery-persistence-adapter)"]
        RepoAdapter["Repository Adapter\n(Version-Checked Save)"]
        ProductEntity["DiscoveryProductEntity\n(JPA @Version)"]
        VariantEntity["DiscoveryProductVariantEntity\n(Relational Membership)"]
    end

    subgraph SourcePorts ["Source Domain Ports (read-only)"]
        CatalogPort["Catalog Port"]
        InventoryPort["Inventory Port"]
        ChannelPort["Sales Channel Port"]
    end

    EventListener --> CmdHandler
    CmdHandler --> ProjectSvc
    ProjectSvc --> ProjectionPolicy
    ProjectSvc --> CatalogPort
    ProjectSvc --> InventoryPort
    ProjectSvc --> ChannelPort
    ProjectSvc --> RepoAdapter
    ProjectionPolicy --> ProjectionModel
    RepoAdapter --> ProductEntity
    RepoAdapter --> VariantEntity
```

### Component Responsibilities

| Architectural Role | Layer Location | Responsibility |
| :--- | :--- | :--- |
| `Projection Service` | `discovery-application` | Resolves affected pairs, re-reads authoritative facts from source ports, delegates to policy for composition, calls repository to persist. |
| `Projection Policy` | `discovery-application` | Pure logic: composes a complete projection record from facts, detects tombstone state, applies watermark comparison. No DI, no side effects. |
| `Projection Model` | `discovery-application` | Immutable Java `record` carrying all facts plus `NEW_PROJECTION_VERSION` sentinel constant (`-1L`). |
| `Repository Adapter` | `discovery-persistence-adapter` | Translates projection records to/from JPA entities, checks version expectations, performs atomic variant membership replacement. |

### Runtime Flow

> Refresh triggered by a catalog presentation change.

```mermaid
sequenceDiagram
    participant Listener as Event Listener
    participant Bus as CommandBus
    participant Handler as Command Handler
    participant Svc as Projection Service
    participant Policy as Projection Policy
    participant Cat as Catalog Port
    participant Inv as Inventory Port
    participant SC as Sales Channel Port
    participant Repo as Repository Adapter
    participant DB as Discovery Database

    Listener->>Bus: dispatch(RefreshProduct)
    Bus->>Handler: handle(command)
    Handler->>Svc: execute(command)
    Svc->>Repo: findByProduct(productId)
    Repo->>DB: SELECT existing projections
    Svc->>Cat: findProduct(productId)
    Cat-->>Svc: ProductFacts + VariantFacts
    loop Each affected (channelId, productId)
        Svc->>Policy: isOlderThanStored(watermark, stored)
        Policy-->>Svc: false (proceed)
        Svc->>Inv: available(sku, merchantId, channelId)
        Inv-->>Svc: Availability(qty, backorder)
        Svc->>SC: isEnabled(channelId)
        SC-->>Svc: true
        Svc->>Policy: compose(facts...)
        Policy-->>Svc: DiscoveryProductProjection
        Svc->>Repo: save(projection)
        Repo->>DB: UPDATE + replace memberships
    end
```

---

## 4. Technical Specification & Contracts

Normative specification. An implementation is compliant only when all MUST / MUST NOT statements are satisfied.

### Normative Rules

- **R-01:** The projection row **MUST** store solely currency-agnostic catalog presentation and inventory facts per `(sales_channel_id, product_id)` pair: channel, product, merchant, and category identifiers; product title, slug, description, thumbnail, options, and status; publication membership; channel status; derived stock badge; update watermark; and optimistic-lock version.
- **R-02:** Variant JSON facts **MUST** include variant ID, SKU, title, status, option values, available quantity, inventory management, backorder configuration, and factual stock state.
- **R-03:** The discovery projection and its queries **MUST NOT** persist, request, or return currency codes, monetary amounts, compare-at prices, price ranges, or commercial buyability.
- **R-04:** Description selection **MUST** deterministically choose the first nonblank description ordered by ID. Thumbnail selection **MUST** choose the first usable media URL ordered by rank then ID.
- **R-05:** Inventory quantity **MUST** sum eligible, non-negative quantities from active locations routed to the channel. Untracked variants (`manageInventory = false`) **MUST** report zero available quantity without fabricating stock.
- **R-06:** Publication membership **MUST** be retained even when a variant is inactive; query visibility **MUST** separately require at least one ACTIVE published variant.

### Storage & Data Contract

```mermaid
erDiagram
    discovery_product {
        bigint id PK "auto-increment"
        bigint version "JPA @Version"
        varchar sales_channel_id "NOT NULL"
        varchar product_id "NOT NULL"
        varchar merchant_id
        varchar category_id
        varchar title
        varchar slug "nullable (cleared on tombstone)"
        text description
        varchar thumbnail "max 2048"
        varchar product_status "max 64"
        boolean published "NOT NULL"
        boolean channel_enabled "NOT NULL"
        boolean has_stock "NOT NULL"
        jsonb options "NOT NULL (ProductOption list)"
        jsonb variants "NOT NULL (VariantFact list)"
        timestamp updated_at "NOT NULL (watermark)"
    }

    discovery_product_variant_membership {
        bigint id PK
        bigint discovery_product_id FK
        varchar variant_id "indexed"
        varchar sku "indexed"
        varchar variant_status
    }

    discovery_product ||--o{ discovery_product_variant_membership : "has variants"
```

### Configuration Knobs

| Binding / constant | Implemented default | Unit / format | Description |
| :--- | :--- | :--- | :--- |
| `DiscoveryProductProjection.NEW_PROJECTION_VERSION` | `-1L` | Constant (long) | Sentinel denoting expected absence on first insert; independent of JPA `@Version` starting at `0`. |
| `DiscoveryProductEntity.@Version` | JPA auto-managed (starts `0`) | Auto-increment | Optimistic lock version maintained by Hibernate; incremented on every flush. |

Implementation references: [`DiscoveryProductProjection`](../../discovery-application/src/main/java/com/grab/discovery/application/model/projection/DiscoveryProductProjection.java), [`DiscoveryProjectionPolicy`](../../discovery-application/src/main/java/com/grab/discovery/application/policy/DiscoveryProjectionPolicy.java), [`DiscoveryProductEntity`](../../discovery-persistence-adapter/src/main/java/com/grab/discovery/adapter/persistence/entity/DiscoveryProductEntity.java).
