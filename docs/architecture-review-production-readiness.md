# Architecture Review & Production Readiness

| | |
|---|---|
| **Date** | 2026-11-13 |
| **Branch reviewed** | `feature/migration` |
| **Scope** | Whole repository: module layout, runtime topology, persistence, eventing, security, ops, CI/CD |
| **Related docs** | [002 Service Architecture](system-design/002-ecommerce_service_architecture_system_design.md), [003 Service Communication](system-design/003-service_communication_and_protocol_system_design.md), [feat_010 Multi-schema isolation](features/feat_010-multi-schema-database-isolation-connection-budgeting.md), [feat_011 CQRS pipeline](features/feat_011-decoupled-service-communication-cqrs-pipeline.md) |

---

## 1. Executive Summary

The codebase is a **modular monolith** (Spring Boot 3.5 / Java 21 / Spring Modulith) with strict hexagonal layering, group-scoped CQRS buses, schema-per-module PostgreSQL isolation and a transactional outbox. The *design* is production-grade and is already shaped so modules can be extracted into services later (Tier-2 Java ports as gRPC drop-ins, outbox-based choreography).

The *runtime* is not yet production-ready. The design assumes a single process; several mechanisms break or degrade as soon as you run **more than one replica**:

| # | Blocker | Why it matters |
|---|---|---|
| B1 | JWT RSA key pair is generated in memory at startup (`IdentityKeyConfig`) | Every restart invalidates all tokens; with 2+ replicas a token signed by pod A fails on pod B. |
| B2 | ~185 max DB connections per app instance (17 Hikari pools) vs. Postgres `max_connections=300` | You can't run 2 replicas without exhausting Postgres. Needs PgBouncer or pool consolidation. |
| B3 | 30 `@Scheduled` jobs with no distributed lock | Outbox pollers, cleanup and workflow sweepers run on every replica. |
| B4 | Outbox claim uses `PESSIMISTIC_WRITE` without `SKIP LOCKED` | Replicas block each other on the same rows instead of splitting the work. |
| B5 | `hbm2ddl.auto=update` default on catalog, inventory, identity | Hibernate can change a production schema without review. Flyway must be the only schema writer. |
| B6 | No Actuator / Micrometer / tracing | No health probes, metrics, or traces, so you can't operate or autoscale it. |
| B7 | Insecure defaults in config (`admin/123`, `s3admin/s3secret`, JDWP debug agent on by default in compose, seeds enabled) | One missing env var gives you a weak production deployment. |

Section 6 has a phased plan that fixes these, and Section 5 compares alternative technologies (PgBouncer, caching, event brokers and others).

---

## 2. Current Architecture Snapshot

### 2.1 Module topology

```
                         ┌────────────────────────── app (single Spring Boot jar) ──────────────────────────┐
  HTTP ─► Platform BFFs  │  platform/seller (sellercentral)      platform/marketplace                       │
                         │        │ Tier-2 Java ports (port.message.*Port)                                  │
                         │        ▼                                                                         │
                         │  account        merchandising       channel             checkout     workflow   │
                         │  ├ identity     ├ catalog           ├ region            ├ cart                   │
                         │  ├ merchant     ├ inventory         ├ saleschannel      ├ order (off)            │
                         │  └ customer     └ pricing           └ discovery (read)  └ payment (off)          │
                         │        │  group-scoped CommandBus / QueryBus + @{Group}Transactional             │
                         │        ▼                                                                         │
                         │  *-adapter-persistence (JPA, Specifications, per-module Flyway)                  │
                         │  outbox-infrastructure (DualQueueOutboxRelay) · workflow-infrastructure          │
                         │  storage-adapter-s3 · logger-slf4j · framework (CQRS, Id, errors, outbox SPI)    │
                         └──────────────┬───────────────────────────────────────────────┬───────────────────┘
                                        ▼                                               ▼
                         PostgreSQL 16 (schema per module, 17 Hikari pools)     S3 (SeaweedFS in dev)
```

* **Build:** Maven multi-module (`domain` → `application` → `adapter-persistence` per bounded context). `order-*` and `payment-*` are commented out of the reactor. `store/` is a legacy module that is not in the reactor but still holds `ArchitectureTest` and JWT code. Decide whether to delete or migrate it.
* **Communication tiers** (per AGENTS.md):
  1. REST ingress only in BFFs.
  2. In-process Java ports → group CQRS buses.
  3. Async integration events via per-schema transactional outbox → in-process `@EventListener` (89 listener sites).
* **Persistence:** 17 datasources (`account`, `channel`, `checkout`, `merchandising`, `catalog`, `inventory`, `identity`, `merchant`, `saleschannel`, `pricing`, `storefrontquery`, `region`, `customer`, `cart`, `order`, `payment`, `workflows`). Each module has its own Flyway history table and location.
* **Outbox:** a row is written in the business transaction. After commit, a *hot queue* (in-memory `ArrayBlockingQueue`, 2 workers) dispatches it immediately. A *cold* poller (`fixed-delay 5s`, batch 20, claim timeout 120s, retention 7d) recovers anything the hot path dropped. Serialization is JSON (`JsonOutboxEventSerializer`). A Java-serialization variant also exists in `framework`.
* **Workflow:** a custom event-driven engine (`EventDrivenWorkflowEngine`) with a JPA store and a 30s sweeper.
* **Security:** in-house JWT (RS256 via jjwt). Refresh-token sessions are stored in Postgres with pessimistic locks. A security/role manifest is published between modules.

### 2.2 Strengths (keep these)

| Area | Assessment |
|---|---|
| Layering & boundaries | Very strong. Domain is framework-free, ports/adapters are explicit, Modulith + ArchUnit enforce boundaries. |
| CQRS | Command and query paths are cleanly split, with group-scoped buses and read projections (`*QueryPort` → `*View`). |
| Data isolation | Schema-per-module with no cross-schema joins. Modules can later move to separate databases without code changes. |
| Reliable eventing | Transactional outbox with hot + cold path, claim timeouts, retention cleanup. This is the correct foundation. |
| Migrations | Flyway per module already exists. |
| Container | Multi-stage build, non-root user, container-aware JVM flags. |
| Extraction path | Tier-2 ports designed as gRPC stubs, and the outbox can be repointed to a broker. |

---

## 3. Detailed Findings

Severity: 🔴 must fix before go-live · 🟠 fix before scaling or soon after launch · 🟡 improvement.

### 3.1 Security

| Sev | Finding | Location | Recommendation |
|---|---|---|---|
| 🔴 | RSA key pair generated per process | `app/.../identity/internal/config/IdentityKeyConfig.java` | Load the private key from a secret store (Vault / AWS KMS / K8s Secret). Publish the public keys as a **JWKS** with `kid` so keys can rotate (current + next). Validators should accept multiple `kid`s. |
| 🔴 | Default credentials in `application-dev.yml` and compose (`admin/123`, `s3admin/s3secret`) | `application-dev.yml`, `docker-compose.yml` | Add an `application-prod.yml` with **no defaults** (`${VAR}` without a fallback) so startup fails fast. Load secrets from Vault, AWS Secrets Manager, or SOPS / External Secrets Operator. |
| 🔴 | `JAVA_TOOL_OPTIONS` enables JDWP on `*:5005` by default | `docker-compose.yml` | Use it in dev only. Never ship it in a production manifest. |
| 🟠 | No rate limiting / brute-force protection on login & refresh | identity BFF endpoints | Rate-limit at the edge (gateway) and in the app (Bucket4j + Redis) per IP and account. Add account lockout and backoff. |
| 🟠 | Seeds enabled by default (`*_SEED_ENABLED=true`) | compose / yml | Default to `false` in prod. Seed reference data through Flyway `R__`/versioned scripts. |
| 🟠 | Swagger UI publicly exposed | `application.yml` springdoc | Disable it in prod or put it behind admin auth. |
| 🟡 | CORS origins / cookie `Secure=false` defaults | compose | In prod, require `SECURITY_API_COOKIE_SECURE=true` and `SameSite=Strict` or `Lax`, and allow-list origins explicitly. |
| 🟡 | Dependency & image scanning | CI | Add OWASP dependency-check or Snyk, plus Trivy/Grype on the image, and produce an SBOM (CycloneDX). |

### 3.2 Database & connection management

| Sev | Finding | Recommendation |
|---|---|---|
| 🔴 | **Connection math:** 17 pools, Σ max ≈ **185** connections per instance (15 × 10 + 20 + 15). Postgres `max_connections=300`. Two replicas plus a migration job plus admin tools exceed the limit. | Put **PgBouncer** (transaction mode) in front of Postgres and shrink the Hikari pools to 2–5 each. Alternatively, consolidate pools to one per *service group* (account / merchandising / channel / checkout / workflow = 5 pools), which matches the AGENTS.md group design. Ideally do both. |
| 🔴 | `hbm2ddl.auto=update` default for catalog, inventory, identity | Set `validate` everywhere and let Flyway own the DDL. Add a CI check that fails if any profile uses `update`/`create`. |
| 🟠 | Flyway runs inside app startup for 17 schemas | Run migrations as a separate **Job / init container** (`flyway migrate`) before rollout. Disable `spring.flyway.enabled` in the app or use `validate`-only mode. Follow the expand/contract pattern for zero-downtime changes (see feat_008). |
| 🟠 | Text search uses `LIKE` specifications (`DiscoveryProductSearchSpecification`, inventory searches) | Short term: add `pg_trgm` GIN indexes. Medium term: move storefront discovery to a search engine (see 5.5). |
| 🟠 | No read-replica routing | Discovery/storefront queries are read-heavy. Route the `@{Group}ReadTransactional` path to a replica (e.g. `AbstractRoutingDataSource` keyed on read-only TX, or a separate read pool). The CQRS split makes this easy. |
| 🟡 | Hikari tuning | Set `max-lifetime` a few minutes **below** the PgBouncer/DB idle timeout. Set `leak-detection-threshold` in staging. `connection-timeout` of 30s is too long for a web request; use 3–5s so the app fails fast. |
| 🟡 | Backups / PITR | Use a managed Postgres (RDS / Cloud SQL / Aurora) or CloudNativePG / Patroni with WAL archiving (pgBackRest / WAL-G). Run restore drills. |

### 3.3 Outbox, events & scheduling

| Sev | Finding | Recommendation |
|---|---|---|
| 🔴 | 30 `@Scheduled` jobs, no distributed lock. Every replica runs every poller, cleanup and workflow sweeper. | Add **ShedLock** (JDBC provider in each group schema, or Redis) for cleanup and sweeper jobs. Outbox pollers can stay on every node *if* claims use `SKIP LOCKED` (next row). |
| 🔴 | Outbox claim = `PESSIMISTIC_WRITE` (`SELECT … FOR UPDATE`) without `SKIP LOCKED` | Add the hint `jakarta.persistence.lock.timeout = -2` (Hibernate maps it to `SKIP LOCKED` on Postgres). Then replicas split batches instead of serializing on them. |
| 🟠 | Hot queue is in-memory. Full queue → events dropped and recovered by the 5s poller. Pod kill → in-flight events recovered only after the 120s claim timeout. | Acceptable (at-least-once), but **consumers must be idempotent**. Add an `inbox`/processed-event table keyed by event id per consumer. Add metrics for queue depth, drops, lag and retry counts. |
| 🟠 | Consumers are in-process `@EventListener`s, so delivery is tied to this JVM | This is fine while the app is a monolith. Once you extract a module or need fan-out to external systems (search indexer, analytics, notifications), relay the outbox to a broker (see 5.3). Keep the outbox as the source of truth; only the relay target changes. |
| 🟠 | Retry policy / dead-letter | Add max attempts, exponential backoff, and a `FAILED`/DLQ status with an alert. Add an admin endpoint or runbook to replay. |
| 🟡 | `JavaSerializationOutboxEventSerializer` exists in framework | Remove or deprecate it. Java serialization is a security and versioning risk. Standardize on JSON with an explicit `schemaVersion` header (and later Avro/Protobuf with a schema registry). |
| 🟡 | Custom workflow engine | Fine for now. If sagas grow (order → payment → fulfillment), evaluate **Temporal** before hand-rolling timers, compensation and versioning (see 5.6). |

### 3.4 Observability & operations

| Sev | Finding | Recommendation |
|---|---|---|
| 🔴 | No `spring-boot-starter-actuator`, no Micrometer | Add Actuator with `health` (liveness/readiness groups that include each datasource), `prometheus`, and `info`. Expose them on a separate management port. |
| 🔴 | Docker `HEALTHCHECK` is `nc -z localhost 8080` (port open ≠ healthy) | Use `/actuator/health/liveness` and `/actuator/health/readiness` as K8s probes. |
| 🟠 | No distributed tracing (only `shared/tracing` scaffolding and modulith-observability) | Add **OpenTelemetry** (Java agent or Micrometer Tracing + OTLP exporter). Propagate trace context through outbox headers (`OutboxEventHeaders`) so async hops stay linked. |
| 🟠 | Logs written to a file (`logs/store.log`) | In containers, log **JSON to stdout** (Logback encoder / ECS format) and include `traceId`, `spanId`, `module`, `tenant/merchantId`. Ship logs with Fluent Bit / Vector. |
| 🟠 | No `application-prod.yml` | Create one with prod-safe values (no SQL logging, `validate` DDL, seeds off, swagger off, graceful shutdown). |
| 🟡 | Graceful shutdown | Set `server.shutdown=graceful` and `spring.lifecycle.timeout-per-shutdown-phase=30s`, and make sure the outbox relay drains (it already has `drainTimeout`). |
| 🟡 | Virtual threads | Java 21 is in use. Consider `spring.threads.virtual.enabled=true` for the web tier. **Only** do this after PgBouncer or with pool limits in place, because virtual threads remove the natural request-concurrency cap. |

### 3.5 CI/CD

The current pipeline has two steps: `mvnw package -DskipTests` and then `mvnw test`.

| Needed | Notes |
|---|---|
| Run tests in a single `verify` | Includes Testcontainers JPA tests plus ArchUnit/Modulith tests (R22). |
| Static analysis | SpotBugs / Error Prone, plus Sonar quality gate. |
| Security | Dependency scan, Trivy image scan, gitleaks secrets scan, SBOM. |
| Image build & push | Tag images with the git SHA and sign them (cosign). |
| Migration job | `flyway validate` in CI against an ephemeral Postgres, and `flyway migrate` as a pre-deploy job. |
| Deploy | Helm / Kustomize with rolling or canary deploys (Argo Rollouts / Flagger), gated on readiness and error-rate SLOs. |
| Performance | k6/Gatling smoke on staging for login, catalog browse, add-to-cart and checkout. |

### 3.6 Code / architecture hygiene

* **`store/` legacy module**: not in the reactor, but `ArchitectureTest` and JWT code live there. Move the ArchUnit rules into `app/src/test` (next to `ModularityTests`) so they actually run in CI, then delete `store/`.
* **`order`/`payment` disabled**: checkout is incomplete. Payment needs idempotency keys, a webhook inbox and a PCI scope review before launch.
* **Pool config duplication**: both group-level pools (`merchandising`, `checkout`, `account`, `channel`) and module-level pools (`catalog`, `inventory`, …) are defined. Pick one model. Group-level matches AGENTS.md §3 and divides connections by ~3.
* **Async request timeout 360s** (`spring.mvc.async.request-timeout`): too long for production. Use ≤30s and move long-running work to workflows.

---

## 4. Target Production Topology (recommended)

```
 Internet
    │
 ┌──▼───────────────────┐
 │ CDN + WAF            │  CloudFront / Cloudflare (static media from S3, edge caching of discovery GETs)
 └──┬───────────────────┘
 ┌──▼───────────────────┐
 │ API Gateway / Ingress│  rate limit, TLS, auth pre-check (Kong / Envoy Gateway / NGINX / AWS ALB)
 └──┬───────────────────┘
 ┌──▼──────────────────────────────────────────┐
 │ grab-app  (K8s Deployment, N≥3 replicas)    │  HPA on CPU + p95 latency
 │  • Caffeine L1 cache                        │
 │  • ShedLock for singleton jobs              │
 │  • OTel agent → Collector                   │
 └──┬──────────────┬───────────────┬───────────┘
    │              │               │
 ┌──▼─────────┐ ┌──▼──────────┐ ┌──▼──────────────────┐
 │ PgBouncer  │ │ Redis/Valkey│ │ Broker (phase 3)     │
 │ (txn mode, │ │ (L2 cache,  │ │ Kafka / Redpanda /   │
 │  2 replicas)│ │  rate limit,│ │ RabbitMQ / NATS      │
 └──┬─────────┘ │  sessions)  │ │ fed by outbox relay  │
    │           └─────────────┘ │ or Debezium CDC      │
 ┌──▼────────────────────────┐  └──────────────────────┘
 │ PostgreSQL primary + read │
 │ replica(s), PITR backups  │  schema-per-module
 └───────────────────────────┘
 Observability: Prometheus + Grafana · Loki · Tempo/Jaeger · Alertmanager → on-call
 Secrets: Vault / AWS Secrets Manager via External Secrets Operator
```

---

## 5. Technology Options & Alternatives

### 5.1 Database connection pooling / proxy

| Option | Type | Pros | Cons / caveats | Fit |
|---|---|---|---|---|
| **PgBouncer** | Lightweight C pooler | Industry standard, tiny footprint, transaction pooling multiplexes thousands of client connections onto ~50–100 server connections. Since 1.21 it supports protocol-level prepared statements (`max_prepared_statements`). | Single-threaded per process (run several or use `so_reuseport`). Transaction mode breaks session state: `SET`, session advisory locks, `LISTEN/NOTIFY`, temp tables. No read/write split. | ✅ **Recommended default.** The app uses Hibernate `default_schema` instead of `SET search_path`, so it is transaction-mode compatible. |
| **PgCat** | Rust pooler | Multi-threaded, built-in read-replica load balancing, sharding, prepared statements, query mirroring | Younger project, smaller community | Good if you want read/write splitting at the proxy instead of in the app. |
| **Odyssey** (Yandex) | Multi-threaded C pooler | High throughput, per-route pool settings | Less documentation, smaller ecosystem | Niche. |
| **Supavisor** (Supabase) | Elixir pooler | Cloud-native, multi-tenant, handles massive connection counts | Heavier, better for many-tenant SaaS | Overkill here. |
| **Pgpool-II** | Pooler + LB + HA | Read/write split, failover | Complex, historically fragile, heavier | ❌ Avoid unless you need its HA features. |
| **AWS RDS Proxy / Cloud SQL Auth Proxy / Azure PgBouncer** | Managed | Zero ops, IAM auth, failover-aware | Cost, cloud lock-in. RDS Proxy *pins* sessions on some statements, which reduces multiplexing. | ✅ If you run on managed Postgres. |
| **Pool consolidation only** (no proxy) | App change | No new infra | Doesn't solve horizontal scaling long-term | Do this *in addition*. |

**PgBouncer configuration notes for this codebase**

* `pool_mode = transaction`, with one database entry per logical DB (`product_merchandising`, `order_management`, `identity_account`, `channel_context`).
* Postgres JDBC behind PgBouncer < 1.21: add `prepareThreshold=0` to every JDBC URL. On ≥ 1.21: set `max_prepared_statements = 200` and keep JDBC defaults.
* Keep Hikari pools small (`maximum-pool-size` 3–5). Set Hikari `max-lifetime` below PgBouncer's `server_lifetime`.
* Outbox `SELECT … FOR UPDATE SKIP LOCKED` works in transaction mode. Session advisory locks do **not**, so if you adopt ShedLock, use the JDBC table-based lock provider, not advisory locks.
* Run Flyway migrations **directly against Postgres** (bypassing PgBouncer), or use session mode for the migration user, because some DDL and `CREATE INDEX CONCURRENTLY` need a session.

### 5.2 Caching

| Layer | Option | Use for | Notes |
|---|---|---|---|
| L1 in-process | **Caffeine** (via Spring Cache) | Region, currency, sales-channel config, category tree, security manifest / role catalog, JWKS | Microsecond latency, no infra. Invalidate through the existing integration events (`@EventListener` → `cache.evict`). Short TTL as a safety net. |
| L2 distributed | **Redis 7 / Valkey 8** | Discovery/product views, pricing lookups, cart (optional), rate-limit buckets, idempotency keys, refresh-token denylist | Valkey is the BSD-licensed Redis fork (AWS ElastiCache and GCP Memorystore support it). Use Redis Cluster or Sentinel for HA. |
| L2 alt | **Dragonfly** | Same as Redis, higher throughput per node | Redis-protocol compatible and multi-threaded. Younger, smaller managed offering. |
| L2 alt | **Hazelcast / Apache Ignite** | Embedded distributed cache / near-cache | JVM-native with near-cache. Adds cluster membership complexity to app pods. |
| L2 alt | **Memcached** | Pure simple KV cache | No data structures or persistence, so less versatile than Redis. |
| HTTP/edge | **CDN (CloudFront/Cloudflare/Fastly)** + `Cache-Control`/`ETag` | Public storefront GETs (discovery, product detail), media | Huge offload for read-heavy storefront traffic. Region- or channel-aware cache keys (`Vary` / custom key). |
| Hibernate L2 | Hibernate 2nd-level cache (JCache + Caffeine/Redisson) | Hot reference entities | Usually unnecessary given CQRS read views. Prefer explicit caching at `*QueryAdapter`. |

**Where caching belongs in this architecture.** Cache in the **persistence query adapters** (`*QueryAdapter` implementing `*QueryPort`), or as a decorator port, so handlers stay unaware of it and no rule is violated. Evict on the integration events the module already publishes. Never cache on the write path.

### 5.3 Event streaming / messaging (outbox relay target)

Today the outbox is relayed in-process. When you need cross-process consumers, these are the options:

| Option | Model | Pros | Cons | Fit |
|---|---|---|---|---|
| **Apache Kafka** (MSK, Confluent, Strimzi) | Durable partitioned log | Replay, ordering per key, huge ecosystem (Connect, Streams, Schema Registry), Debezium native | Operational weight, partition planning | ✅ Best long-term for event choreography plus analytics / search indexing. |
| **Redpanda** | Kafka-API compatible, C++ | No ZooKeeper/JVM, simpler ops, low latency, same clients | Some Kafka-ecosystem gaps, licensing (BSL) | ✅ Kafka semantics with less ops overhead. |
| **RabbitMQ** (with Streams) | Broker / queues | Mature, flexible routing, DLQ built in, easy ops, Spring AMQP | Classic queues have no replay (Streams add it), lower throughput than Kafka | ✅ Good if you need task/command queues more than event logs. |
| **NATS JetStream** | Lightweight streaming | Very simple ops, tiny footprint, KV + object store, low latency | Smaller enterprise ecosystem | Good for lean infra. |
| **AWS SNS + SQS / EventBridge** | Managed | Zero ops, fan-out, DLQ | Lock-in, no ordering across keys (FIFO limits), no long replay | ✅ If all-in on AWS. |
| **Google Pub/Sub** | Managed | Zero ops, global | Lock-in | If on GCP. |
| **Apache Pulsar** | Log + queue | Multi-tenancy, tiered storage | Complex (BookKeeper) | Overkill. |

**Relay mechanism options**

1. **Keep the current polling relay** and publish to the broker in `OutboxWorkHandler`. Smallest change, and the hot-queue logic stays as is.
2. **Debezium CDC** (Kafka Connect or Debezium Server → any broker) reading the outbox tables with the *Outbox Event Router* SMT. Lowest latency, and no pollers in the app. Requires logical replication (`wal_level=logical`) and a replication slot per database, so monitor slot lag.
3. **Spring Modulith Event Externalization** (`spring-modulith-events-kafka/amqp`). Fits since Modulith is already a dependency, but overlaps with the custom outbox. Choose one.

Recommendation: stay in-process for launch. When the first module is extracted, or search indexing is needed, adopt **Kafka/Redpanda with Debezium**, or option 1 for simplicity. Use event keys = aggregate id for ordering. Use JSON + `schemaVersion` first, then move to Avro/Protobuf with a schema registry (Confluent / Apicurio).

### 5.4 Distributed locking / scheduling

| Option | Notes |
|---|---|
| **ShedLock** (JDBC provider) | ✅ Simplest. Uses one `shedlock` table per group schema. Works behind PgBouncer. |
| ShedLock (Redis provider) | Use once Redis is in place. |
| **Quartz (clustered JDBC)** | Use for complex schedules or misfire handling. Heavier. |
| **JobRunr** | Background jobs with dashboard, retries, persistence in Postgres. Nice for "run later" tasks. |
| `SELECT … FOR UPDATE SKIP LOCKED` | Already the right primitive for outbox and work-queue tables. Just add the hint. |
| K8s CronJob | For batch jobs that don't need the app context (cleanup, reports). |

### 5.5 Search (storefront discovery)

| Option | Pros | Cons |
|---|---|---|
| **Postgres FTS + `pg_trgm`** | No new infra. Fine for an MVP catalog. | Weak relevance tuning, facets are costly at scale. |
| **OpenSearch / Elasticsearch** | Facets, relevance, multi-language analyzers, aggregations | Ops-heavy. Needs an indexing pipeline (outbox/CDC → indexer). |
| **Meilisearch / Typesense** | Fast, typo-tolerant, simple ops, good for product search | Less flexible analytics / aggregations. |
| **Algolia** (SaaS) | Best-in-class DX, zero ops | Cost at scale, lock-in. |

The discovery module is already a projection (`storefrontquery` schema). Feeding a search engine from the same integration events is a natural extension.

### 5.6 Workflow / saga orchestration

| Option | Notes |
|---|---|
| Current `EventDrivenWorkflowEngine` | Fine for provisioning-type flows. |
| **Temporal** | Durable execution, timers, retries, versioning, visibility UI. Strong choice for checkout → payment → fulfillment sagas. |
| **Camunda 8 / Zeebe** | BPMN-modelled, good when business users own the processes. |
| **Netflix Conductor (Orkes)** | JSON-defined workflows, microservice orchestration. |

### 5.7 API gateway, edge & rate limiting

| Option | Notes |
|---|---|
| **Kong / Envoy Gateway / APISIX** | Rate limiting, JWT validation at the edge, canary routing. |
| **AWS ALB + WAF / API Gateway** | Managed. Add WAF managed rules (OWASP, bot control). |
| **Cloudflare** | CDN + WAF + bot management + rate limiting in one. |
| **Bucket4j (+ Redis)** | In-app per-user / per-merchant limits that the edge can't know about. |
| **Resilience4j** | Circuit breakers, bulkheads and timeouts on outbound calls (payment gateway, S3, future gRPC). |

### 5.8 Identity & secrets

| Need | Options |
|---|---|
| Token signing keys | Vault Transit / AWS KMS (sign without exporting the key), or K8s Secret + JWKS endpoint with rotation |
| Full IdP (if you stop maintaining in-house auth) | **Keycloak**, Auth0/Okta, AWS Cognito, Zitadel. Spring Resource Server validates the JWT against the JWKS. |
| Secret delivery | Vault + Vault Agent, **External Secrets Operator** (AWS SM / GCP SM / Vault), SOPS for GitOps |

### 5.9 Object storage

| Env | Option |
|---|---|
| Dev | SeaweedFS (current) or MinIO |
| Prod | AWS S3 / GCS (S3 interop) / Cloudflare R2 (no egress fees). Serve through a CDN. Keep presigned uploads (already implemented). Add lifecycle rules and virus scanning for user uploads (ClamAV Lambda / GuardDuty Malware Protection). |

### 5.10 Observability stack

| Concern | OSS | Managed |
|---|---|---|
| Metrics | Prometheus + Grafana (or VictoriaMetrics for scale) | Grafana Cloud, Datadog, New Relic |
| Traces | OpenTelemetry → Tempo / Jaeger | Datadog APM, Honeycomb, AWS X-Ray |
| Logs | Loki / OpenSearch via Fluent Bit / Vector | Datadog, CloudWatch, Elastic Cloud |
| Errors | Sentry (self-host) | Sentry SaaS |
| Alerting | Alertmanager → PagerDuty / Opsgenie | — |

**Key SLIs to instrument:**
* HTTP p95/p99 latency and 5xx rate per BFF route.
* Hikari `active` / `pending` / `timeout` per pool.
* PgBouncer `cl_waiting`, `avg_wait_time`.
* Outbox `pending count`, `oldest pending age`, `hot-queue drops`, `retries`, `failed`.
* Workflow stuck/compensating instances.
* JVM GC pause and heap.
* Login failure rate.

### 5.11 Runtime platform

| Option | Notes |
|---|---|
| **Kubernetes (EKS/GKE/AKS)** + Helm/Kustomize + Argo CD | Most flexible. HPA, PodDisruptionBudgets, rollouts. |
| **AWS ECS Fargate / Google Cloud Run** | Less ops. Cloud Run needs care with background pollers (set min instances and CPU always-allocated). |
| Postgres | Managed (RDS/Aurora/Cloud SQL/AlloyDB) or **CloudNativePG** operator on K8s. |
| JVM startup | Consider **CDS / AppCDS** or Spring AOT. Use GraalVM native only if cold start matters (reflection-heavy MapStruct/Hibernate needs testing). |

---

## 6. Production Roadmap

### Phase 0: Go-live blockers (must do)

- [ ] Persistent JWT signing keys from a secret store, a JWKS endpoint, and `kid` rotation (B1)
- [ ] PgBouncer (transaction mode) and/or consolidate to group-level pools. Size the connection budget per replica (B2)
- [ ] ShedLock on every non-idempotent `@Scheduled` job (B3)
- [ ] `SKIP LOCKED` on outbox claim queries (B4)
- [ ] `ddl-auto=validate` everywhere. Run Flyway as a pre-deploy job (B5)
- [ ] Actuator + Prometheus + liveness/readiness probes. JSON logs to stdout (B6)
- [ ] `application-prod.yml` with no insecure defaults: seeds off, swagger off, JDWP off, secure cookies (B7)
- [ ] Rate limiting on auth endpoints
- [ ] Idempotent event consumers (processed-event / inbox table)
- [ ] Postgres backups + PITR with a tested restore
- [ ] CI: `verify` with Testcontainers + ArchUnit, image scan, SBOM, signed image

### Phase 1: Scale & resilience (first months)

- [ ] OpenTelemetry tracing across HTTP → bus → outbox → listener
- [ ] Caffeine L1 cache for reference data, evicted via integration events
- [ ] Redis/Valkey for distributed rate limiting, idempotency keys, hot read views
- [ ] Read-replica routing for `ReadTransactional` / discovery queries
- [ ] CDN in front of storefront GET endpoints and media
- [ ] `pg_trgm` indexes, or a search engine for discovery
- [ ] Outbox DLQ, alerting and replay tooling
- [ ] Load tests (k6/Gatling) with SLO targets. Tune pool sizes from the results.
- [ ] Re-enable `order`/`payment` with idempotency keys, a webhook inbox and a PCI review

### Phase 2: Evolve toward services (when team or traffic demands)

- [ ] Broker (Kafka/Redpanda or RabbitMQ) fed by the outbox relay or Debezium CDC
- [ ] Schema registry and versioned event contracts
- [ ] Extract the first module, likely `discovery` (read-heavy, already a projection) or `identity`. Swap its Tier-2 Java ports for gRPC stubs.
- [ ] Separate databases per extracted service (schemas are already isolated)
- [ ] Temporal (or similar) for checkout/payment/fulfillment sagas
- [ ] Optional: replace in-house auth with Keycloak / a managed IdP

---

## 7. Appendix: Connection Budget Example

Current state (per app instance, defaults):

```
15 pools × 10 + checkout 20 + merchandising 15 = 185 max connections
Postgres max_connections = 300 → 1 instance + ~115 headroom; 2 instances = 370 ❌
```

With group-level pools and PgBouncer:

```
App: 5 group pools × 5 = 25 client conns/instance → 6 instances = 150 client conns to PgBouncer
PgBouncer: default_pool_size ≈ 20 per (db,user), 4 dbs → ≈ 80 server conns to Postgres
Postgres max_connections = 200 (headroom for replication, Flyway, admin, monitoring)
```

Rule of thumb for server-side pool size: `connections ≈ (CPU cores × 2) + effective_spindle_count` on the DB host. More connections than that usually *reduces* throughput.
