# Multi-Regional Contextual Product Discovery System Design

## 1. The Problem

### What's Not Working?
- **Public Channel Spoofing**: The storefront browse API currently accepts `?salesChannelId=...` directly in query parameters, allowing anonymous clients to probe arbitrary seller channels, bypass commercial boundaries, and enumerate private channels.
- **Single-Currency Channel Trap**: The existing `discovery_product` projection stores a single static currency and price range (`currency`, `min_price`, `max_price`), preventing a single sales channel (e.g., a merchant's global website) from serving multiple geographic regions in their local legal currencies (e.g., Singapore in `SGD` and Malaysia in `MYR`).
- **Combinatorial Explosion & Write Amplification**: Pre-calculating and storing materialized search documents for every channel, region, and currency permutation causes exponential data growth ($10,000 \text{ products} \times 5 \text{ channels} \times 10 \text{ regions} = 500,000 \text{ documents}$), causing any exchange rate or pricing rule change to trigger massive database re-indexing storms.
- **Binary & Misleading Availability**: Products that run out of stock or lack regional price configurations either disappear entirely (causing unexpected `404 Not Found` errors) or display `0.00` prices, breaking search engine crawl rankings, invalidating bookmarks, and confusing shoppers.

### What's at Stake?
Failing to implement this design exposes commercial channels to unauthorized scraping, degrades customer trust through price discrepancies, causes CDN cache poisoning across national borders, and severely harms SEO ranking due to ephemeral 404 errors on temporarily depleted inventory.

---

## 2. The Proposed Design

### 2.1 Architecture Overview
The proposed subsystem introduces a **Two-Tier CQRS Read Model** powered by **Pre-Query Edge Context Resolution** and **Vectorized Request-Time Batch Regional Pricing**.

Tier 1 stores currency-agnostic catalog presentation facts, while Tier 2 computes regional prices in memory on demand.

```mermaid
flowchart TD
    Client["Storefront Client"] -->|"X-Publishable-Key + countryCode"| EdgeResolver["Edge Context Resolver"]
    EdgeResolver -->|"1. Validate Token"| SalesChannel["Sales Channel Service"]
    EdgeResolver -->|"2. Resolve Region & Currency"| Region["Region Service"]
    EdgeResolver -->|"ShoppingContext"| DiscoveryRead["Catalog Projection (Tier 1)"]
    DiscoveryRead -->|"Catalog Presentation Facts"| BatchEnricher["Batch Price Enricher (Tier 2)"]
    BatchEnricher -->|"Vectorized Quote Query"| Pricing["Pricing Engine"]
    BatchEnricher -->|"Contextual Offer Response"| Client
```

### 2.2 Key Components & Services
| Component / Service | Core Responsibilities | Interactions & Collaborators |
|:---|:---|:---|
| **Edge Context Resolver** | Resolves public tokens to channels, validates country or resolves default region, and builds `ShoppingContext`. | Calls `Sales Channel Service` for key verification and `Region Service` for country-to-region mapping. |
| **Sales Channel Service** | Manages channel aggregate, publishable key lifecycle with SHA-256 digests, and enabled `ChannelRegion` bindings. | Provides `PublishableKeyQuery` port to `Edge Context Resolver`. |
| **Region Service** | Acts as sole authority for ISO country membership and settlement currency codes (`currencyCode`). | Provides `RegionQueryPort` to `Edge Context Resolver` and pricing services. |
| **Catalog Projection Engine (Tier 1)** | Maintains denormalized `discovery_product` documents keyed by `(sales_channel_id, product_id)` with presentation facts only. | Consumes domain events from `Catalog` and `Inventory`; serves paged search results to `Batch Price Enricher`. |
| **Batch Price Enricher (Tier 2)** | Performs single-trip vectorized price evaluation for all variant IDs on a page using 4-tier rule precedence. | Calls `Pricing Engine` via `PricingQuoteQuery.quoteBatch` and constructs final customer response. |

### 2.3 Data Model
The subsystem decouples catalog facts from volatile pricing rules to prevent Cartesian explosion and write amplification.

#### Key Entities & Value Objects
- **`PublishableKey` (Aggregate Root)**: Stores channel ID, merchant label, 64-character SHA-256 `token_digest`, and public `display_prefix` (`pk_live_...`).
- **`ChannelRegion` (Entity in Sales Channel)**: Associates a channel with enabled regions (`sales_channel_id`, `region_id`, `is_enabled`, `is_default`).
- **`RegionCountry` (Entity in Region)**: Maps ISO 3166-1 country codes exclusively to one `Region` with settlement `currencyCode`.
- **`ShoppingContext` (Value Object)**: Immutable tuple `(salesChannelId, regionId, countryCode, currencyCode, locale)` resolved at request arrival.
- **`DiscoveryProductDocument` (Tier 1 Projection)**: Keyed by `(sales_channel_id, product_id)`; retains title, slug, media, options, and stock badges while stripping static currency and prices.

#### Schema & Persistence Structure
```sql
CREATE TABLE sales_channel_publishable_keys (
    id VARCHAR(255) PRIMARY KEY,
    sales_channel_id VARCHAR(255) NOT NULL,
    label VARCHAR(100) NOT NULL,
    token_digest VARCHAR(64) NOT NULL UNIQUE,
    display_prefix VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP(6) WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_sales_channel_keys_channel
        FOREIGN KEY (sales_channel_id) REFERENCES sales_channels(uuid)
);

CREATE INDEX idx_sales_channel_keys_channel
    ON sales_channel_publishable_keys (sales_channel_id);
```

#### Ubiquitous Language & Ownership Matrix
| Bounded Context | Aggregate / Root | Owns | References by ID | Explicitly Does NOT Own |
|:---|:---|:---|:---|:---|
| **Sales Channel** | `SalesChannel`, `PublishableKey` | Channel identity, key lifecycle, enabled `ChannelRegion` bindings. | `merchantId` | Product catalog, settlement currencies, warehouse stock. |
| **Region** | `Region` | Legal market boundaries, member countries, settlement `currencyCode`. | ISO 3166-1, ISO 4217 | Sales channels, commercial prices, shipping rates. |
| **Catalog** | `Product` | Product taxonomy, media, variants, publication status. | `salesChannelId` | Prices, legal currency definitions, stock levels. |
| **Pricing** | `PriceSet` | Monetary amounts, currencies, 4-tier precedence `PriceRule`s. | `variantId` | Product titles, channels, shopping carts. |
| **Discovery** | `DiscoveryProductProjection` | Composite read model for catalog browse and PDP presentation. | `channelId`, `productId` | Transactional mutations, source inventory allocations. |

### 2.4 Key Flows & Interactions

#### Flow 1: Storefront Request & Batch Pricing Sequence
The client supplies credentials in the HTTP header, the edge resolves context, and Tier 2 enriches catalog facts in a single vectorized call.

```mermaid
sequenceDiagram
    autonumber
    actor Shopper as Storefront Client
    participant Resolver as Edge Context Resolver
    participant SalesChannel as Sales Channel Port
    participant Region as Region Port
    participant Discovery as Tier 1 Catalog Projection
    participant Pricing as Tier 2 Pricing Engine

    Shopper->>Resolver: GET /products?countryCode=SG (Header: X-Publishable-Key)
    Resolver->>SalesChannel: resolveKey(rawToken)
    SalesChannel-->>Resolver: Validated Channel ("sc_01")
    Resolver->>Region: findByCountry("SG")
    Region-->>Resolver: Region ("reg_sg", currency: "SGD")
    Resolver->>SalesChannel: verifyServesRegion("sc_01", "reg_sg")
    SalesChannel-->>Resolver: Confirmed Served

    Resolver->>Discovery: search(query, channel="sc_01")
    Discovery-->>Resolver: Page<CatalogFacts> (45 variants)
    Resolver->>Pricing: quoteBatch([var_1..var_45], currency="SGD", region="reg_sg")
    Pricing-->>Resolver: Map<VariantId, QuotedPrice>
    Note over Resolver: Recalculate price ranges & availability states
    Resolver-->>Shopper: 200 OK (Contextual JSON)
```

#### Flow 2: Variant Availability State Machine
Products remain visible under HTTP 200 to protect SEO while variants declare explicit availability codes.

```mermaid
stateDiagram-v2
    [*] --> ContextCheck
    ContextCheck --> NOT_AVAILABLE_IN_REGION: Channel does not serve region
    ContextCheck --> PriceCheck: Channel serves region

    PriceCheck --> PRICE_UNAVAILABLE: No regional price configured
    PriceCheck --> StockCheck: Regional price resolved

    StockCheck --> SOLD_OUT: inStock <= 0 AND backorder == false
    StockCheck --> AVAILABLE: inStock > 0 OR backorder == true
```

#### Flow 3: Publishable Key Cryptographic Lifecycle
Tokens use SHA-256 one-way hashing to guarantee defense-in-depth against credential leaks.

```mermaid
flowchart LR
    subgraph Provisioning ["Key Provisioning"]
        RNG["SecureRandom (32 bytes)"] --> Token["Token (pk_live_...)"]
        Token -->|"Returned once"| Client["Storefront Admin"]
        Token --> Hash["SHA-256 Digest"]
        Hash --> DB["Digest stored in DB"]
    end

    subgraph Runtime ["Runtime Resolution"]
        Req["X-Publishable-Key Header"] --> RuntimeHash["SHA-256 Digest"]
        RuntimeHash --> Lookup["Indexed DB Lookup"]
        Lookup --> Context["Active Channel Context"]
    end
```

---

## 3. Implementation Approach

### 3.1 Technology & Tooling Choices
| Technology / Pattern | Selection Rationale |
|:---|:---|
| **Java 21 & Spring Modulith** | Enforces strict hexagonal boundary isolation and verifies cross-module architectural invariants via `@ApplicationModuleTests`. |
| **PostgreSQL & Flyway** | Delivers ACID transaction safety, partial unique indexes for single default regions, and reproducible versioned schema migrations. |
| **Two-Tier CQRS Pattern** | Decouples static catalog presentation facts from volatile regional price rules, eliminating Cartesian storage explosion. |
| **SHA-256 Token Hashing** | Ensures defense-in-depth by persisting only cryptographic digests, preventing credential exposure during database inspections or leaks. |
| **RFC 7807 Problem Details** | Standardizes error representations across modules with uniform status codes (`401`, `400`, `404`, `422`). |

---

## 4. Why This Approach (Technical Rationale)
| Evaluation Metric | Selected Design (Two-Tier CQRS) | Alternative A (Materialized Multi-Region Docs) | Alternative B (Live Joins Across Write BCs) |
|:---|:---|:---|:---|
| **Simplicity** | **High**: Clean boundaries; catalog handles media/text; pricing engine handles quotes. | **Low**: High coupling between pricing events and discovery projector. | **Low**: Complex ad-hoc distributed coordination. |
| **Performance** | **Fast ($< 15\text{ ms}$)**: Single indexed projection read plus single vectorized in-memory batch quote. | **Fastest ($< 5\text{ ms}$)**: Single flat document fetch from storage. | **Unacceptable ($> 200\text{ ms}$)**: $N+1$ synchronous network queries across 4 modules. |
| **Scalability** | **Linear ($O(P \times C)$)**: Zero write amplification on price, tax, or exchange rate adjustments. | **Exponential ($O(P \times C \times R)$)**: Re-indexing storms across hundreds of thousands of rows. | **Linear**: Minimal storage overhead but severe write lock contention. |
| **Maintainability** | **High**: Complies with Spring Modulith boundaries; domain models remain framework-free. | **Moderate**: Requires complex event choreography to synchronize cross-module updates. | **Violated**: Direct cross-boundary coupling creates cascading failure risks. |
| **Security** | **Robust**: Cryptographic SHA-256 key hashing and rejection of public `salesChannelId` spoofing. | **Weak**: Exposes query parameters susceptible to channel enumeration. | **Weak**: Relies on complex distributed permission propagation. |

---

## 5. Implementation Phases


Phase 01: Sales Channel Publishable Key Lifecycle
  - Implement PublishableKey aggregate, SecureRandom generator, SHA-256 hashing, and management REST APIs.
  - [link](../features/feat_003-sales-channel-publishable-keys.md)

Phase 02: Regional Channel Binding & Authority
  - Establish Region as sole currency authority, create ChannelRegion entity, and add default region indexes.
  - [link](../features/feat_001-default_saleschannel_provisioning.md)

Phase 03: Vectorized Batch Regional Pricing Engine
  - Implement QuoteVariantPricesService with 4-tier precedence rule evaluation and single-trip batch calculations.
  - [link](../features/feat_002-discovery_product.md)

Phase 04: Edge Shopping-Context Resolution & Discovery Wiring
  - Implement ShoppingContextResolver, decouple Tier 1 catalog projections, and wire Modulith event listeners.
  - [link](../features/feat_002-discovery_product.md)

Phase 05: Storefront API Contract Cutover & Availability Precedence
  - Enforce X-Publishable-Key header, reject salesChannelId with 400, and cut over to BuyableOfferResponse schema.
  - [link](../features/feat_002-discovery_product.md)

Phase 06: Automated Verification & Zero-Downtime Rollout
  - Execute multi-region quote verification, dual-USD isolation tests, and zero-downtime database deployment.
  - [link](../features/feat_003-sales-channel-publishable-keys.md)

