# Tech Spec: Order Service Group Persistence

- **Summary:** Defines the shared relational schema, connection pooling, and Flyway migration stream for the Order Service Group (`cart`, `order`, `payment`) on the shared database instance using quoted schema `"order"`.
- **Type:** Platform Infrastructure
- **Feature Spec:** [Multi-Schema Database Isolation & Connection Budgeting](../../features/feat_010-multi-schema-database-isolation-connection-budgeting.md)

---

## 1. Problem & Decision

### What breaks without this?

If cart, order, and payment contexts competed for database connections against browsing or catalog traffic, a flash sale or heavy search volume would exhaust the connection pool and drop active checkouts. Furthermore, `ORDER` is an ANSI SQL and PostgreSQL reserved keyword; unquoted schema usage triggers syntax errors across database migration scripts and ORM mappings.

### Decision

| Option | Decision | Why |
|:-------|:---------|:----|
| **Shared Quoted Schema `'"order"'` for Cart, Order, and Payment** | Adopted | Consolidates checkout lifecycle entities under a high-priority, dedicated connection pool (`OrderHikariPool`), protecting transactional checkout operations from search/catalog traffic while grouping tightly coupled transaction steps. |
| Split Cart, Order, and Payment into 3 Separate Schemas | Rejected | Premature separation; cart-to-order conversion and payment records benefit from local transactional coordination during the first phases of checkout. |
| Unpartitioned Monolithic Database | Rejected | Allows non-critical queries to starve mission-critical payment and order processing connections. |

### Trade-offs

| Benefits | Trade-offs |
|:---------|:-----------|
| Transactional checkout flows have a guaranteed, dedicated connection pool that cannot be exhausted by external read traffic. | All Flyway migration scripts, configurations, and queries MUST explicitly quote the schema name `'"order"'`. |
| Cart conversion, order creation, and payment recording execute within local transactional boundaries. | Even within schema `'"order"'`, table ownership must remain strictly separated by bounded context with no cross-context foreign keys. |

---

## 2. How It Works

### Core Concept

The Order Service Group (`com.grab.app.order`) operates its own dedicated PostgreSQL database instance named `order_db`. Within `order_db`, each bounded context owns a dedicated relational schema:
- `cart` domain $\rightarrow$ schema `'cart'`
- `order` domain $\rightarrow$ schema `'"order"'` (quoted SQL identifier)
- `payment` domain $\rightarrow$ schema `'payment'`

The service group uses a dedicated `OrderHikariPool` (max 20) targeting `order_db`, while each domain executes independent Flyway migration streams into its dedicated schema.

### How It Works Step by Step

| Step | What Happens | Why |
|:-----|:-------------|:----|
| 1 | Order Service boots and establishes `OrderHikariPool` targeting PostgreSQL schema `'"order"'`. | Provides reserved connection headroom for mission-critical write transactions. |
| 2 | Flyway runs migrations from `db/migration/order`, quoting the schema name and writing to `'"order"'.flyway_schema_history`. | Applies DDL changes cleanly without SQL keyword parser collisions. |
| 3 | Use cases interact with aggregates via their respective repository adapters (`CartRepositoryAdapter`, `OrderRepositoryAdapter`, `PaymentRepositoryAdapter`). | Preserves single-table responsibility per bounded context. |
| 4 | State changes and outbound domain events (`OrderPlaced`, `PaymentAuthorized`) commit atomically within `@OrderTransactional`. | Protects business transactions and ensures at-least-once outbox delivery. |

### Schema Table Allocation & Ownership

| Bounded Context | Owned Tables | Description |
|:----------------|:-------------|:------------|
| **Cart** | `cart_header`, `cart_line_item`, `cart_discount` | Active shopper baskets, line items, and promotional discounts. |
| **Order** | `order_header`, `order_line_item`, `order_fulfillment`, `order_adjustment` | Committed commercial orders, tracking status, and line fulfillments. |
| **Payment** | `payment_intent`, `payment_transaction`, `payment_refund` | Payment authorizations, gateway references, and refund records. |
| **Shared Infrastructure** | `outbox_record`, `flyway_schema_history` | Transactional outbox event queue and Flyway schema version history. |

---

## 3. How We Use It

### Component Responsibilities

| Component | Role | What It Does |
|:----------|:-----|:-------------|
| **`OrderPersistenceConfig`** | Spring Configuration | Configures `DataSource`, `HikariConfig`, `EntityManagerFactory`, and `TransactionManager` targeting `'"order"'`. |
| **`CartRepositoryAdapter`** | Outbound Adapter | Persists and loads shopping cart aggregates. |
| **`OrderRepositoryAdapter`** | Outbound Adapter | Persists order state transitions (`PENDING`, `CONFIRMED`, `FULFILLED`, `CANCELLED`). |
| **`PaymentRepositoryAdapter`** | Outbound Adapter | Manages idempotent payment transactions and gateway authorizations. |
| **`OrderOutboxProducer`** | Outbox Infrastructure | Inserts checkout integration events into `'"order"'.outbox_record`. |

### Configuration Specification

#### 1. Group Database Configuration (Order DB Instance)

```yaml
grab:
  order:
    datasource:
      jdbc-url: jdbc:postgresql://${ORDER_DB_HOST:localhost}:${ORDER_DB_PORT:5432}/${ORDER_DB_NAME:order_db}
      username: ${ORDER_DB_USER:order_user}
      password: ${ORDER_DB_PASSWORD:order_secret}
      driver-class-name: org.postgresql.Driver
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        pool-name: OrderHikariPool
```

#### 2. Domain Module Configurations (Schema per Domain on Order DB Instance)

Existing domain module configuration keys are preserved, reference the parent group datasource, and target their dedicated schema:

```yaml
cart:
  datasource:
    jdbc-url: ${grab.order.datasource.jdbc-url}?currentSchema=cart
    username: ${grab.order.datasource.username}
    password: ${grab.order.datasource.password}
    driver-class-name: ${grab.order.datasource.driver-class-name}
    hikari:
      pool-name: OrderHikariPool
  flyway:
    schemas: cart
    default-schema: cart
    table: flyway_schema_history
    locations: classpath:db/migration/cart

order:
  datasource:
    jdbc-url: ${grab.order.datasource.jdbc-url}?currentSchema="order"
    username: ${grab.order.datasource.username}
    password: ${grab.order.datasource.password}
    driver-class-name: ${grab.order.datasource.driver-class-name}
    hikari:
      pool-name: OrderHikariPool
  flyway:
    schemas: '"order"'
    default-schema: '"order"'
    table: flyway_schema_history
    locations: classpath:db/migration/order

payment:
  datasource:
    jdbc-url: ${grab.order.datasource.jdbc-url}?currentSchema=payment
    username: ${grab.order.datasource.username}
    password: ${grab.order.datasource.password}
    driver-class-name: ${grab.order.datasource.driver-class-name}
    hikari:
      pool-name: OrderHikariPool
  flyway:
    schemas: payment
    default-schema: payment
    table: flyway_schema_history
    locations: classpath:db/migration/payment
```

---

## 4. Implementation Rules

> These are **non-negotiable contracts**. An implementation is compliant only when **all** rules are satisfied.

- **RULE-ORD-DB-01: Quoted Schema Identifier Required**
  The schema name `'"order"'` MUST ALWAYS be enclosed in double quotes in YAML configuration, Flyway migration files, and native SQL snippets.

- **RULE-ORD-DB-02: Schema per Domain Encapsulation**
  Within database instance `order_db`, each bounded context MUST operate strictly within its assigned schema (`cart`, `'"order"'`, `payment`). Bounded contexts MUST NOT declare foreign keys across schemas or cross-join tables.

- **RULE-ORD-DB-03: Zero Cross-Context Foreign Keys**
  Foreign key constraints between `cart_header` and `order_header` or `order_header` and `payment_intent` are prohibited. References MUST be stored as typed domain identifiers (`OrderId`, `PaymentId`).

- **RULE-ORD-DB-04: Dedicated Group Pool & Central Schema Qualification**
  All order service group bounded contexts MUST connect directly to `order_db` via `OrderHikariPool` (`maximum-pool-size: 20`). All JPA entities and queries MUST configure `hibernate.default_schema` (especially `default_schema: '"order"'`) to route queries to their domain schema without hardcoding schemas in `@Table` annotations.

- **RULE-ORD-DB-05: Atomic Local Outbox Commit**
  All domain events MUST be committed to their local schema outbox table (`cart.outbox_record`, `'"order"'.outbox_record`, `payment.outbox_record`) within their domain `@OrderTransactional` boundary.
