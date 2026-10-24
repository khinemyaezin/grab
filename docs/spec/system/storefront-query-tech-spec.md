# Technical Specification: Storefront Query and Discovery Facts

> **Summary:** Stores and queries channel-scoped catalog and inventory facts. Prices and contextual availability are computed in Phase 5.
> **Classification:** CQRS Read Model / Modulith Integration
> **Supporting Modules:** `discovery-application`, `discovery-persistence-adapter`, `store/storefrontquery`
> **Related Feature:** [Discovery Facts, Projection Reconciliation, and Rebuild](../../features/feat_006-discovery-wiring-projection-reconciliation.md)
> **Related Workflow:** [Browse, Project, and Rebuild](../workflows/browse-and-discover-products-workflow.md)
> **System Design:** [SSD-001](../../system-design/001-multi_regional_contextual_discovery_system_design.md)

## 1. Scope

Phase 4 completes facts-only discovery search and product detail queries, event-driven projection refresh, and persisted bounded rebuild. It does not cut product endpoints over to shopping-context resolution or add regional quote/availability composition; those are Phase 5.

The discovery V1 migration is the clean initial schema for an empty discovery database. Existing discovery databases are not reset automatically. No upgrade migration, legacy-data backfill, or dual-read/write mechanism is provided.

## 2. Module boundaries

Catalog and Inventory publish read-only discovery interfaces in their public `port` packages. Their provider adapters delegate to inbound read use cases within module read transactions. `store/storefrontquery` consumes those interfaces through consumer adapters and owns event-to-command wiring. Discovery persistence reads and writes only its own database. Projection, factual query and rebuild services do not call Pricing.

Cross-module events live under `store/shared/events/{catalog,inventory,saleschannel}`. `@EventListener` consumers dispatch local discovery commands through `CommandBus`; command handlers own discovery transactions. A failed listener propagates so the source outbox does not acknowledge an unsuccessful projection update.

## 3. Factual model and search

One row is keyed by `(sales_channel_id, product_id)` and enforces channel-scoped slug uniqueness. Product facts include merchant/category IDs, title, slug, description, thumbnail, options, product status, publication membership, channel status, stock badge, update watermark and optimistic-lock version. Variant JSON contains ID, SKU, title, status, option values, quantity, inventory-management/backorder facts and stock state.

No currency, monetary amount, compare-at amount, range, or buyability is persisted. The relational variant-membership table indexes exact variant ID and SKU. Membership changes are written atomically with the projection. Local foreign keys do not cross module databases.

Search uses an injected JPA specification with optional case-insensitive literal keyword matching against title/description, exact category filtering, pagination and deterministic product-ID ordering. Visibility requires enabled channel, ACTIVE parent product, and at least one ACTIVE published variant. Stock and price do not affect visibility. PDP lookup applies the same visibility conditions and reports an invisible or missing product as `OfferNotFound`.

## 4. Projection rules

Every event is a refresh signal, not a source of authoritative fact values. The use case resolves affected product/channel pairs, deduplicates identifiers, rereads current Catalog, Inventory and Channel facts, then composes a complete projection. Channel-targeted publication changes affect their addressed channel; product-wide, variant, SKU, route, stock, and channel changes also cover existing projections needed to remove stale facts.

Publication membership is retained even when a variant is inactive; query visibility separately requires an active variant. Description selection is the first nonblank description ordered by ID. Thumbnail selection is the first usable media URL ordered by rank then ID. Inventory counts nonnegative eligible quantity from active locations routed to the channel, excluding suspended/discontinued inventory. Backordering is true when an eligible routed item permits it. Untracked variants have no fabricated stock quantity.

Events strictly older than the stored watermark are ignored; equal timestamps reconcile against current source facts. Updates check expected projection version and flush. Optimistic conflicts and concurrent channel/product insert collisions retry at most three times in separate transactions with a fresh reread. Slug conflicts, provider failures, and exhausted retries propagate. Invalidation retains a tombstone and watermark, clears slug, publication and variants, and rechecks current publication before writing.

`NEW_PROJECTION_VERSION` (`-1`) denotes expected absence, independently of database version `0` on a newly inserted row. The write adapter checks both expected existence and version. It executes the local membership delete before inserting replacements and flushes the membership writes within the handler transaction. Cursor and aggregate queries return typed immutable local records rather than positional arrays. Inventory variant-view updates also pass through CommandBus, an inventory transactional handler, an application use case and a local projection repository; projection notifications live under `shared/events/inventory`.

## 5. Persisted rebuild

`storefrontquery.rebuild.on-startup` defaults to `false`; `storefrontquery.rebuild.batch-size` defaults to `100` and must be positive. Startup submits work to a managed executor and does not wait for completion. Each rebuild command processes one batch. The coordinator dispatches subsequent commands after the current handler transaction commits.

One unfinished run is allowed. Its persisted record contains framework run ID, start time/watermark, stage, product/channel cursor, processed and invalidated counts, and optimistic-lock version. Stages are `PUBLICATIONS`, `EXISTING_PROJECTIONS`, and `COMPLETED`. A keyset query enumerates distinct current publication product/channel pairs, then a second keyset pass visits existing projection rows. Batches and checkpoints commit atomically; a failed batch leaves the cursor unchanged. The run resumes after restart and serializes checkpoint advancement. Refresh reuses the same projection use case and preserves rows updated after the run watermark. Publication is rechecked immediately before invalidation. Completed runs and tombstones are retained; retention cleanup is out of scope.

## 6. Schema and verification

The initial discovery migration creates `discovery_product`, indexed exact variant membership, and `discovery_rebuild_run`, with channel/product and channel/slug uniqueness and only local foreign keys. The migration is designed for a fresh empty discovery database; applying it does not reset an existing database.

Persistence tests use PostgreSQL Testcontainers to verify the fresh schema, constraints, exact membership, atomic replacement, visibility and checkpoint persistence. Application tests cover projection policy, search/detail use cases and rebuild resume/races. Store Modulith verification checks public-interface dependencies and shared-event placement. The R21 review checks CQRS layering, immutable records, `Id` usage, MapStruct mapping, transaction boundaries, and absence of price dependencies in factual paths.

## 7. Scope & Phase Boundary (Phase 4 vs Phase 5)

This specification strictly governs **Phase 4 (Discovery Facts, Projection Reconciliation, and Rebuild)**. The delivery contract for Phase 4 is:
- **Facts-Only DTOs:** Responses from `SearchBuyableOffersService` and `GetBuyableOfferBySlugService` contain catalog presentation facts, channel-routed stock badges, and variant inventory facts.
- **No Pricing or Buyability:** Discovery persistence and query services do not depend on, persist, or return currency codes, monetary amounts, compare-at amounts, price ranges, or commercial availability reason codes.
- **Phase 5 Handoff:** All contextual requirements (mandatory `X-Publishable-Key` resolution, legacy parameter rejection, single-trip batch pricing quotation, 4-state availability precedence, dynamic price ranges, and `Cache-Control: no-store`) are specified in the dedicated Phase 5 document:
  - 👉 [Storefront Contextual Cutover & Response Contract Tech Specification](./storefront-contextual-cutover-tech-spec.md)

## 8. Detailed Decision Specifications

This specification serves as the architectural umbrella for Phase 4 discovery facts and storefront queries. For complete design details, rationale, sequence diagrams, and implementation rules, refer to the individual per-decision technical specifications:

- [Discovery Facts-Only Projection](./discovery-facts-only-projection-tech-spec.md) — Currency-agnostic CQRS read model decoupling pricing write amplification from materialized projections.
- [Discovery Event-Driven Reconciliation](./discovery-event-reconciliation-tech-spec.md) — Events as refresh signals, affected target resolution, 19-event-to-6-command mapping, and watermark protection.
- [Discovery Projection Concurrency Control](./discovery-concurrency-control-tech-spec.md) — Optimistic locking with `@Version`, `NEW_PROJECTION_VERSION` (-1) sentinel, and bounded 3-attempt fresh-transaction retry loop.
- [Discovery Rebuild Coordinator](./discovery-rebuild-coordinator-tech-spec.md) — Two-stage keyset-paginated rebuild, checkpoint atomicity, resume across restarts, and rebuild-vs-live safety.
- [Discovery Search Specification & Visibility](./discovery-search-visibility-tech-spec.md) — JPA Criteria specification enforcing channel-scoped visibility, literal keyword escaping (`%`, `_`, `\`), and deterministic ordering.
