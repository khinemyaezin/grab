# Tech Spec: Catalog Service Group Persistence

- **Summary:** Defines the shared relational schema, connection pooling, and Flyway migration stream for the Catalog Service Group (`catalog`, `inventory`, `pricing`) on the shared database instance.
- **Type:** Platform Infrastructure
- **Feature Spec:** [Multi-Schema Database Isolation & Connection Budgeting](../../features/feat_010-multi-schema-database-isolation-connection-budgeting.md)

---

## 1. Problem & Decision

### What breaks without this?

If catalog, inventory, and pricing each maintained disjoint, uncoordinated persistence units or competed with unrelated checkout domains in a monolithic public schema, cross-module table contention and complex multi-database transactions would arise during sellable product provisioning. Conversely, allowing unconstrained cross-domain table joins between pricing and orders prevents independent schema optimization and extraction.

### Decision

| Option | Decision | Why |
|:-------|:---------|:----|
| **Shared Schema `'catalog'` across Catalog, Inventory, and Pricing** | Adopted | Closely related product capabilities share schema `'catalog'` on the shared PostgreSQL instance with a unified connection pool, simplifying atomic product and inventory updates while remaining encapsulated from other service groups. |
| Independent Database Instance per Bounded Context | Rejected | Unnecessary operational overhead and infrastructure cost for closely coupled catalog contexts at current scale. |
| Shared Public Monolith Schema | Rejected | Leaks table structures across unrelated domains (such as orders and accounts), preventing clean modular boundaries. |

### Trade-offs

| Benefits | Trade-offs |
|:---------|:-----------|
| `catalog`, `inventory`, and `pricing` share a single connection pool and Flyway stream, optimizing connection usage. | Internal bounded contexts within the group must enforce table ownership rules in code to prevent cross-context SQL joins. |
| High-velocity catalog and inventory operations are isolated from checkout connection spikes. | Bulk pricing updates or re-indexing operations share connection bandwidth with inventory adjustments. |

---

## 2. How It Works

### Core Concept

The Catalog Service Group (`com.grab.app.catalog`) operates its own dedicated PostgreSQL database instance named `catalog_db`. Within `catalog_db`, each bounded context owns a dedicated relational schema:
- `catalog` domain $\rightarrow$ schema `'catalog'`
- `inventory` domain $\rightarrow$ schema `'inventory'`
- `pricing` domain $\rightarrow$ schema `'pricing'`

The service group shares an optimized `HikariDataSource` connection pool (`CatalogHikariPool`, max 15) targeting `catalog_db`, while each domain executes independent Flyway migration streams into its dedicated schema.

```mermaid
flowchart TB
 subgraph s3["Connection Pool\n max:15"]
        n5["Catalog\nDatasource"]
  end
 subgraph s4["Persistence Unit"]
        n6["Catalog"]
        n8["Pricing"]
        n9["Inventory"]
  end
 subgraph s5["Domain Group"]
        n11["Catalog"]
        n12["Pricing"]
        n13["Inventory"]
  end
    s3 --> n10["Database\n Instance"]
    n11 -- "<span style=text-wrap-mode:>@Transaction</span>" --> n6
    n12 -- "<span style=text-wrap-mode:>@Transaction</span>" --> n8
    n13 -- @Transaction --> n9
    s4 --> s3

    n5@{ shape: rect}
    n6@{ shape: rect}
    n8@{ shape: proc}
    n9@{ shape: rect}
    n11@{ shape: rect}
    n12@{ shape: proc}
    n13@{ shape: rect}
    n10@{ shape: cyl}
```
### How It Works Step by Step

| Step | What Happens | Why |
|:-----|:-------------|:----|
| 1 | Catalog Service initializes the shared `CatalogHikariPool` targeting PostgreSQL schema `'catalog'`. | Sets up isolated database connections with bounded capacity. |
| 2 | Flyway migrates SQL scripts from `db/migration/catalog` and records version history in `catalog.flyway_schema_history`. | Ensures reproducible and versioned DDL updates for all catalog, inventory, and pricing tables. |
| 3 | Use case services execute state changes via intra-domain repository adapters (e.g., `CatalogRepositoryAdapter`, `InventoryRepositoryAdapter`). | Each adapter queries only its own owned tables; cross-context joins are prohibited. |
| 4 | State changes and outbound domain events are committed within `@CatalogTransactional`. | Aggregate state mutations and outbox records are written atomically. |

### Schema Table Allocation & Ownership

| Bounded Context | Owned Tables | Description |
|:----------------|:-------------|:------------|
| **Catalog** | `catalog_product`, `product_variant`, `product_category`, `product_media` | Master product catalog, hierarchy, and presentation facts. |
| **Inventory** | `inventory_item`, `inventory_level`, `inventory_reservation`, `stock_location` | Stock counts, warehouse allocations, and checkout reservations. |
| **Pricing** | `price_list`, `pricing_rule`, `price_tier`, `regional_price_override` | Currency amounts, tiered pricing, and regional price adjustments. |
| **Shared Infrastructure** | `outbox_record`, `flyway_schema_history` | Transactional outbox log and Flyway schema version tracking. |

---

## 3. How We Use It

### Component Responsibilities

| Component | Role | What It Does |
|:----------|:-----|:-------------|
| **`CatalogPersistenceConfig`** | Spring Configuration | Declares `DataSource`, `HikariConfig`, `EntityManagerFactory`, and `TransactionManager` scoped to `'catalog'`. |
| **`CatalogRepositoryAdapter`** | Outbound Adapter | Persists catalog product aggregates and variant definitions. |
| **`InventoryRepositoryAdapter`** | Outbound Adapter | Manages inventory levels and atomic stock reservation locks. |
| **`PricingRepositoryAdapter`** | Outbound Adapter | Persists regional price matrices and rule precedence configurations. |
| **`CatalogOutboxProducer`** | Outbox Infrastructure | Writes domain events (`ProductCreated`, `InventoryReserved`) to `catalog.outbox_record`. |

### Configuration Specification

#### 1. Group Database Configuration (Catalog DB Instance)

```yaml
grab:
  catalog:
    datasource:
      jdbc-url: jdbc:postgresql://${CATALOG_DB_HOST:localhost}:${CATALOG_DB_PORT:5432}/${CATALOG_DB_NAME:catalog_db}
      username: ${CATALOG_DB_USER:catalog_user}
      password: ${CATALOG_DB_PASSWORD:catalog_secret}
      driver-class-name: org.postgresql.Driver
      hikari:
        maximum-pool-size: 15
        minimum-idle: 3
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        pool-name: CatalogHikariPool
```

#### 2. Domain Module Configurations (Schema per Domain on Catalog DB Instance)

Existing domain module configuration keys are preserved, reference the parent group datasource, and target their dedicated schema:

```yaml
catalog:
  datasource:
    jdbc-url: ${grab.catalog.datasource.jdbc-url}?currentSchema=catalog
    username: ${grab.catalog.datasource.username}
    password: ${grab.catalog.datasource.password}
    driver-class-name: ${grab.catalog.datasource.driver-class-name}
    hikari:
      pool-name: CatalogHikariPool
  flyway:
    schemas: catalog
    default-schema: catalog
    table: flyway_schema_history
    locations: classpath:db/migration/catalog

inventory:
  datasource:
    jdbc-url: ${grab.catalog.datasource.jdbc-url}?currentSchema=inventory
    username: ${grab.catalog.datasource.username}
    password: ${grab.catalog.datasource.password}
    driver-class-name: ${grab.catalog.datasource.driver-class-name}
    hikari:
      pool-name: CatalogHikariPool
  flyway:
    schemas: inventory
    default-schema: inventory
    table: flyway_schema_history
    locations: classpath:db/migration/inventory

pricing:
  datasource:
    jdbc-url: ${grab.catalog.datasource.jdbc-url}?currentSchema=pricing
    username: ${grab.catalog.datasource.username}
    password: ${grab.catalog.datasource.password}
    driver-class-name: ${grab.catalog.datasource.driver-class-name}
    hikari:
      pool-name: CatalogHikariPool
  flyway:
    schemas: pricing
    default-schema: pricing
    table: flyway_schema_history
    locations: classpath:db/migration/pricing
```
---

## 4. Implementation Rules

> These are **non-negotiable contracts**. An implementation is compliant only when **all** rules are satisfied.

- **RULE-CAT-DB-01: Schema per Domain Encapsulation**
  Within database instance `catalog_db`, each bounded context MUST operate strictly within its assigned schema (`catalog`, `inventory`, `pricing`). Bounded contexts MUST NOT access tables in another domain's schema.

- **RULE-CAT-DB-02: Zero Cross-Context Joins**
  Even though `catalog_product` and `inventory_item` reside in the same database instance `catalog_db`, cross-context SQL joins or JPA entity mappings between them are strictly prohibited. Relationships MUST be referenced via typed `Id` only.

- **RULE-CAT-DB-03: Dedicated Group Pool & Central Schema Qualification**
  All catalog service group bounded contexts MUST connect directly to `catalog_db` via `CatalogHikariPool` (`maximum-pool-size: 15`). All JPA entities and queries MUST configure `hibernate.default_schema` (`catalog`, `inventory`, `pricing`) to route queries to their domain schema without hardcoding schemas in `@Table` annotations.

- **RULE-CAT-DB-04: Atomic Local Outbox Commit**
  All domain events MUST be written to their respective domain schema outbox table (`catalog.outbox_record`, `inventory.outbox_record`, `pricing.outbox_record`) within their domain `@CatalogTransactional` boundary.
