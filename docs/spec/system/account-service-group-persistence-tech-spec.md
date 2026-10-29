# Tech Spec: Account Service Group Persistence

- **Summary:** Defines the shared relational schema, connection pooling, and Flyway migration stream for the Account Service Group (`identity`, `merchant`, `customer`) on the shared database instance.
- **Type:** Platform Infrastructure
- **Feature Spec:** [Multi-Schema Database Isolation & Connection Budgeting](../../features/feat_010-multi-schema-database-isolation-connection-budgeting.md)

---

## 1. Problem & Decision

### What breaks without this?

If user identity, merchant governance, and customer management were intermingled with high-churn commercial contexts like order carts or browsing catalogs, authentication and merchant onboarding would be vulnerable to table lock contention during large commerce events. Conversely, splitting identity, merchant, and customer into separate physical databases prematurely would introduce complex distributed transactions during seller and customer onboarding flows.

### Decision

| Option | Decision | Why |
|:-------|:---------|:----|
| **Shared Schema `'account'` for Identity, Merchant, and Customer** | Adopted | Consolidates user authentication, merchant records, and customer accounts under a dedicated, secure schema with its own connection pool (`AccountHikariPool`), isolating sensitive user and merchant credentials from public browsing pools. |
| Dedicated Database Instance per Account Context | Rejected | Premature operational complexity; identity, merchant, and customer domains share lifecycle dependencies during user registration. |
| Monolithic Unpartitioned Schema | Rejected | Exposes credential and merchant tables to potential accidental joins and queries from storefront catalog or cart contexts. |

### Trade-offs

| Benefits | Trade-offs |
|:---------|:-----------|
| User credential verification, merchant profile updates, and customer management have dedicated, unstarved database connections. | Bounded contexts within `account` must strictly enforce table isolation through repository adapters without cross-context joins. |
| Sensitive user and merchant tables are encapsulated inside a dedicated PostgreSQL schema with distinct role permissions. | Identity tokens and session tracking share database connection bandwidth with merchant catalog onboarding. |

---

## 2. How It Works

### Core Concept

The Account Service Group (`com.grab.app.account`) operates its own dedicated PostgreSQL database instance named `account_db`. Within `account_db`, each bounded context owns a dedicated relational schema:
- `identity` domain $\rightarrow$ schema `'identity'`
- `merchant` domain $\rightarrow$ schema `'merchant'`
- `customer` domain $\rightarrow$ schema `'customer'`

The service group operates a dedicated `AccountHikariPool` (max 10) targeting `account_db`, while each domain executes independent Flyway migration streams into its dedicated schema.

### How It Works Step by Step

| Step | What Happens | Why |
|:-----|:-------------|:----|
| 1 | Account Service boots and allocates `AccountHikariPool` targeting PostgreSQL schema `'account'`. | Isolates identity and account connections from transactional order bursts. |
| 2 | Flyway migrates scripts from `db/migration/account` and writes history to `account.flyway_schema_history`. | Maintains versioned DDL for credentials, merchant accounts, and customer addresses. |
| 3 | Use cases interact with domain models via their respective repository adapters (`IdentityRepositoryAdapter`, `MerchantRepositoryAdapter`, `CustomerRepositoryAdapter`). | Keeps each domain responsible exclusively for its own persistence tables. |
| 4 | State mutations and account integration events commit atomically within `@AccountTransactional`. | Ensures reliable, at-least-once outbox publishing for account and merchant registrations. |

### Schema Table Allocation & Ownership

| Bounded Context | Owned Tables | Description |
|:----------------|:-------------|:------------|
| **Identity** | `user_credential`, `role_binding`, `auth_session` | User authentication, password hashes, and platform roles. |
| **Merchant** | `merchant_account`, `merchant_profile`, `merchant_setting` | Seller organizations, onboarding status, and store settings. |
| **Customer** | `customer_account`, `customer_profile`, `customer_address` | Shopper identities, profiles, and delivery addresses. |
| **Shared Infrastructure** | `outbox_record`, `flyway_schema_history` | Transactional outbox event store and Flyway version tracking. |

---

## 3. How We Use It

### Component Responsibilities

| Component | Role | What It Does |
|:----------|:-----|:-------------|
| **`AccountPersistenceConfig`** | Spring Configuration | Defines `DataSource`, `HikariConfig`, `EntityManagerFactory`, and `TransactionManager` targeting `'account'`. |
| **`IdentityRepositoryAdapter`** | Outbound Adapter | Persists user credentials, salt hashes, and role bindings. |
| **`MerchantRepositoryAdapter`** | Outbound Adapter | Persists merchant organization details and legal metadata. |
| **`CustomerRepositoryAdapter`** | Outbound Adapter | Manages customer profile records and shipping addresses. |
| **`AccountOutboxProducer`** | Outbox Infrastructure | Inserts account events (`UserRegistered`, `MerchantApproved`) into `account.outbox_record`. |

### Configuration Specification

#### 1. Group Database Configuration (Account DB Instance)

```yaml
grab:
  account:
    datasource:
      jdbc-url: jdbc:postgresql://${ACCOUNT_DB_HOST:localhost}:${ACCOUNT_DB_PORT:5432}/${ACCOUNT_DB_NAME:account_db}
      username: ${ACCOUNT_DB_USER:account_user}
      password: ${ACCOUNT_DB_PASSWORD:account_secret}
      driver-class-name: org.postgresql.Driver
      hikari:
        maximum-pool-size: 10
        minimum-idle: 2
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        pool-name: AccountHikariPool
```

#### 2. Domain Module Configurations (Schema per Domain on Account DB Instance)

Existing domain module configuration keys are preserved, reference the parent group datasource, and target their dedicated schema:

```yaml
identity:
  datasource:
    jdbc-url: ${grab.account.datasource.jdbc-url}?currentSchema=identity
    username: ${grab.account.datasource.username}
    password: ${grab.account.datasource.password}
    driver-class-name: ${grab.account.datasource.driver-class-name}
    hikari:
      pool-name: AccountHikariPool
  flyway:
    schemas: identity
    default-schema: identity
    table: flyway_schema_history
    locations: classpath:db/migration/identity

merchant:
  datasource:
    jdbc-url: ${grab.account.datasource.jdbc-url}?currentSchema=merchant
    username: ${grab.account.datasource.username}
    password: ${grab.account.datasource.password}
    driver-class-name: ${grab.account.datasource.driver-class-name}
    hikari:
      pool-name: AccountHikariPool
  flyway:
    schemas: merchant
    default-schema: merchant
    table: flyway_schema_history
    locations: classpath:db/migration/merchant

customer:
  datasource:
    jdbc-url: ${grab.account.datasource.jdbc-url}?currentSchema=customer
    username: ${grab.account.datasource.username}
    password: ${grab.account.datasource.password}
    driver-class-name: ${grab.account.datasource.driver-class-name}
    hikari:
      pool-name: AccountHikariPool
  flyway:
    schemas: customer
    default-schema: customer
    table: flyway_schema_history
    locations: classpath:db/migration/customer
```

---

## 4. Implementation Rules

> These are **non-negotiable contracts**. An implementation is compliant only when **all** rules are satisfied.

- **RULE-ACC-DB-01: Schema per Domain Encapsulation**
  Within database instance `account_db`, each bounded context MUST operate strictly within its assigned schema (`identity`, `merchant`, `customer`). Bounded contexts MUST NOT access tables in another domain's schema.

- **RULE-ACC-DB-02: Zero Cross-Context Foreign Keys**
  Foreign key constraints between `user_credential` and `merchant_account` or `customer_account` are prohibited. References MUST be maintained using typed IDs (`UserId`, `MerchantId`, `CustomerId`).

- **RULE-ACC-DB-03: Dedicated Group Pool & Central Schema Qualification**
  All account service group bounded contexts MUST connect directly to `account_db` via `AccountHikariPool` (`maximum-pool-size: 10`). All JPA entities and queries MUST configure `hibernate.default_schema` (`identity`, `merchant`, `customer`) to route queries to their domain schema without hardcoding schemas in `@Table` annotations.

- **RULE-ACC-DB-04: Atomic Local Outbox Commit**
  All domain events MUST be committed to their respective domain schema outbox table (`identity.outbox_record`, `merchant.outbox_record`, `customer.outbox_record`) within their domain `@AccountTransactional` boundary.
