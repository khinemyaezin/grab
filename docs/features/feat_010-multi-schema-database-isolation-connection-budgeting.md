# Feature: Multi-Schema Database Isolation & Connection Budgeting

> **System Design:** [E-Commerce Service Architecture & Platform Modernization](../system-design/002-ecommerce_service_architecture_system_design.md)

---

## 1. What & Why

### What does this feature do?

This feature establishes an **instance-per-service-group** and **schema-per-domain** database isolation architecture across all commerce services. Rather than a single shared database, each service group operates its own independent database instance (`catalog_db`, `order_db`, `account_db`, `channel_db`). Within each group's database instance, every bounded context domain resides in its own isolated relational schema:
- **Catalog DB Instance** (`catalog_db`): schema `catalog`, schema `inventory`, schema `pricing`.
- **Order DB Instance** (`order_db`): schema `cart`, schema `"order"` (quoted SQL identifier), schema `payment`.
- **Account DB Instance** (`account_db`): schema `identity`, schema `merchant`, schema `customer`.
- **Channel DB Instance** (`channel_db`): schema `saleschannel`, schema `region`, schema `discovery` (with `discovery` using a dedicated connection pool `DiscoveryHikariPool`).

Furthermore, it enforces exclusive schema and table ownership, zero cross-database or cross-schema foreign keys or joins, isolated Flyway migration pipelines per schema, and mathematical connection budgeting per service group database instance.

### Why is it needed?

A single monolithic database creates severe blast radiuses: high-volume browsing or unindexed search queries consume all database connections, starving critical checkout and payment write operations. Moreover, when domains lack schema-level boundaries, database tables become tightly coupled through joins and foreign keys, preventing independent zero-downtime schema evolution and making microservice operations impossible. Partitioning physical database instances per service group and schemas per domain guarantees complete connection and schema autonomy.

### What's in scope / out of scope?

| In Scope | Out of Scope |
|:---------|:-------------|
| Dedicated database instances per service group: `catalog_db`, `order_db`, `account_db`, `channel_db` | Automated cloud provisioning scripts (Terraform / CloudFormation) |
| Dedicated relational schema per domain: `catalog`, `inventory`, `pricing`; `cart`, `"order"`, `payment`; `identity`, `merchant`, `customer`; `saleschannel`, `region`, `discovery` | Automated read-replica failover management |
| Independent connection pool management per service group instance | Business aggregate data migration ETL pipelines |
| Dedicated separate database connection and pool (`DiscoveryHikariPool`) for `discovery` | Redis / cache invalidation strategies |
| Isolated Flyway migration history tables (`{schema}.flyway_schema_history`) per domain schema | NoSQL document database configurations |
| Mathematical connection budgeting formula per database instance | Frontend React / Next.js client state |
| Prohibition of cross-database/cross-schema foreign keys, joins, and distributed 2PC | Business domain entity field definitions |
| Atomic transactional outbox table persistence within each domain's local schema transaction | Physical cluster hardware networking |

---

## 2. Acceptance Criteria

| # | Criteria | How to Verify |
|:--|:---------|:--------------|
| AC-1 | Each service group MUST operate its own dedicated database instance (`catalog_db`, `order_db`, `account_db`, `channel_db`), and each domain within a group MUST be isolated into its own dedicated schema (`schema-per-domain`). | Spring Boot context startup tests verifying distinct JDBC URLs and schema configurations across domains. |
| AC-2 | The `discovery` domain service in the `channel` group MUST configure a dedicated separate connection pool (`DiscoveryHikariPool`), isolating search queries from administrative channel connections. | Datasource configuration test verifying distinct `DataSource` and `HikariPool` bean instances for channel and discovery. |
| AC-3 | The schema name `"order"` in the Order DB instance MUST be enclosed in quoted identifiers in all configuration and Flyway migration scripts to prevent SQL syntax collisions with the SQL reserved keyword `ORDER`. | Database initialization and Flyway migration integration tests against PostgreSQL. |
| AC-4 | Each domain schema MUST execute its own isolated Flyway migration pipeline with a dedicated history table (`{schema}.flyway_schema_history`). | Testcontainers PostgreSQL integration tests verifying independent migration execution per domain schema. |
| AC-5 | Foreign keys, joins, and database-level constraints across database instances or schemas MUST NOT exist; relationships across bounded contexts MUST be maintained via identifier references only. | Automated PostgreSQL schema linting script asserting zero cross-schema foreign keys in `information_schema`. |
| AC-6 | Each service group MUST configure an isolated `HikariDataSource` connection pool (`CatalogHikariPool`, `OrderHikariPool`, `AccountHikariPool`, `ChannelHikariPool`, and dedicated `DiscoveryHikariPool`) connecting directly to its PostgreSQL database instance, with explicit pool bounds (`maximum-pool-size`) preventing cross-service connection exhaustion. | Datasource configuration tests verifying dedicated HikariCP pool instances with configured maximum pool sizes and timeouts directly targeting group database instances. |
| AC-7 | Every transactional outbox entry MUST be written to the domain's local schema outbox table (`{schema}.outbox_record`) within the same local database transaction as the business aggregate state mutation. | Transaction rollback integration tests verifying that if aggregate persistence fails, the outbox record is also rolled back. |
| AC-8 | Overriding an individual domain's database URL, credentials, or pool settings MUST NOT require configuration changes in any other domain service. | Configuration isolation test spinning up an individual domain service targeting an override database URL. |

---

## 3. Solution Design

### How does this feature work?

> The architecture enforces **Database Instance per Service Group** and **Schema per Domain**, ensuring that each service group owns its relational database and connection pool, while each domain is strictly isolated into its own schema.


### Key Design Decisions

| Decision | Choice | Rationale |
|:---------|:-------|:----------|
| **Database Architecture** | Database Instance per Service Group | Complete physical and I/O isolation per functional group; failure or load spikes in catalog browsing cannot crash order placement. |
| **Domain Partitioning** | Schema per Domain within Group Instance | Each domain owns its dedicated schema namespace; eliminates table naming collisions and prevents cross-domain entity joins. |
| **Outbox Scope** | One Outbox Table per Domain Schema (`{schema}.outbox_record`) | Guarantees atomic transaction commits between domain aggregate state changes and event publishing without cross-schema locking. |
| **Discovery Connection Isolation** | Dedicated Separate Connection (`DiscoveryHikariPool`) | Shields administrative channel management from consumer search traffic spikes. |
| **Configuration Hierarchy** | Group Instance Defaults with Per-Domain Schema & Overrides | Domains inherit group connection settings by default while targeting their individual schemas, with full override flexibility. |
| **Connection Pooling** | Direct HikariCP Pools per Service Group | Pods connect directly to each group database instance via bounded HikariCP pools (`maximum-pool-size: 10–20`), keeping infrastructure simple without external proxies. |
| **Central Schema Qualification** | Hibernate Default Schema per Domain | Generates schema-qualified SQL statements (`{schema}.{table}`), keeping entity `@Table` annotations clean while enforcing domain schema isolation. |
| **Migration Management** | Isolated Flyway Streams per Domain Schema | Enables domains to evolve DDL scripts independently without blocking peer domains. |
| **Cross-Context Integrity** | ID References Only (No Foreign Keys) | Enforces clean domain boundaries and guarantees zero data refactoring when services are physically relocated. |

---

## 4. Implementation Specifications

### Domain Specs (Data Models & Business Rules)

- None; this feature governs relational infrastructure and connection pooling. Aggregate data structures are specified in module-level domain specifications.

### Workflow Specs (Process Flows)

- None; database connection management and migration executions are platform infrastructure mechanisms.

### Tech Specs (Technology Decisions)

- [Multi-Schema Database Isolation & Connection Budgeting Tech Spec](../spec/system/multi-schema-database-isolation-tech-spec.md)
- [Catalog Service Group Persistence Tech Spec](../spec/system/catalog-service-group-persistence-tech-spec.md)
- [Order Service Group Persistence Tech Spec](../spec/system/order-service-group-persistence-tech-spec.md)
- [Account Service Group Persistence Tech Spec](../spec/system/account-service-group-persistence-tech-spec.md)
- [Channel Service Group Persistence Tech Spec](../spec/system/channel-service-group-persistence-tech-spec.md)
