# Workflow: Browse, Project, and Rebuild Discovery Facts

> **Purpose:** Keep channel-scoped product facts current and serve factual browse/detail reads.
> **Starts When:** A storefront product read, a source integration event, or an authorized rebuild is requested.
> **Successful Result:** A facts-only product page/detail or a converged projection without changing source aggregates.
> **Participating Domains:** Storefront Query · Catalog · Inventory · Sales Channel
> **Status:** Phase 4 Factual Path (Facts-only browse, event projection refresh, and rebuild coordinator)
> **Related Feature:** [Discovery Facts, Projection Reconciliation, and Rebuild](../../features/feat_006-discovery-wiring-projection-reconciliation.md)
> **Related Technical Specification:** [Storefront Query and Discovery Facts](../system/storefront-query-tech-spec.md)
> **Related Phase 5 Workflow:** [Browse Contextual Storefront Offers](./browse-contextual-storefront-offers-workflow.md)

## 1. Product read path (Factual Discovery)

| ID | Step | Owner | Action | Completion |
| :--- | :--- | :--- | :--- | :--- |
| Q-01 | Dispatch search or detail query | Storefront Query | Map the current endpoint inputs into an immutable CQRS query. | Query is dispatched without a write repository. |
| Q-02 | Read visible facts | Discovery | Search a channel-scoped page or resolve a slug through the discovery query port. | Page or visible PDP is returned; missing/invisible PDP raises `OfferNotFound`. |
| Q-03 | Map facts response | Storefront Query | Map presentation and inventory facts to the current REST representation. | Facts-only response is assembled. |

Visibility requires an enabled channel, an ACTIVE parent product and at least one ACTIVE published variant. Sold-out products remain visible. 

> [!NOTE]
> This read path is strictly currency-agnostic facts only. Dynamic regional pricing, `X-Publishable-Key` verification, commercial availability codes, and contextual HATEOAS links belong to Phase 5 and are documented in [Browse Contextual Storefront Offers Workflow](./browse-contextual-storefront-offers-workflow.md).

## 2. Projection refresh path

| ID | Step | Owner | Action | Completion |
| :--- | :--- | :--- | :--- | :--- |
| D-01 | Receive integration event | Storefront Query | An `@EventListener` maps the event to a local projection command and dispatches through `CommandBus`. | Command handling begins; source payload is treated as a refresh signal. |
| D-02 | Find affected pairs | Discovery | Deduplicate product/channel pairs from current source facts and existing rows. | Targeted publication changes stay channel-scoped; deletions include existing projections. |
| D-03 | Read current facts | Catalog / Inventory / Sales Channel | Read product presentation, publication membership, routed inventory, and channel state through public query interfaces. | Source-of-truth values are available without Pricing. |
| D-04 | Reconcile facts | Discovery | Ignore strictly older updates; otherwise compose and atomically save facts plus exact variant membership or update a tombstone. | Version-checked local transaction commits or propagates failure. |

Description and media selection are deterministic. Publication membership is retained even for inactive variants; the query filter still requires an active variant. Untracked variants have no fabricated stock quantity. Invalidation clears slug and variant membership while retaining the row and watermark, and checks current publication before writing.

## 3. Rebuild path

| ID | Stage | Action | Checkpoint behavior |
| :--- | :--- | :--- | :--- |
| B-01 | `PUBLICATIONS` | Keyset-page distinct current published product/channel pairs and refresh each through the projection use case. | Commit cursor and processed count with the batch. |
| B-02 | `EXISTING_PROJECTIONS` | Keyset-page existing projection pairs, refresh current facts, and recheck publication before invalidation. | Commit cursor, processed count and invalidated count with the batch. |
| B-03 | `COMPLETED` | Retain completed run metadata and finish the coordinator loop. | No source facts or projection rows are deleted for retention. |

One unfinished run is allowed. Each command handles one bounded batch; the startup coordinator dispatches the next command only after the previous transaction commits. The run stores its ID, start watermark, stage, last committed product/channel cursor, counts and optimistic-lock version. A failed batch rolls back writes and checkpoint advancement. A later startup resumes the unfinished run. Newer event projections are preserved by the run-start watermark. The startup feature remains opt-in and executes on a managed executor.

## 4. Events and recovery

| Signal family | Producer | Discovery action |
| :--- | :--- | :--- |
| Publication, product/variant status and presentation changes | Catalog | Refresh addressed channel or all affected product/channel pairs. |
| Stock movement and location/channel route changes | Inventory | Refresh exact SKU matches or the routed channel's projected products. |
| Channel enable/disable | Sales Channel | Refresh existing projections for the channel. |

Shared integration event records live under `store/shared/events/{module}`. Pricing change events do not refresh Discovery facts.

Projection writes use a fresh transaction per retry and attempt optimistic or concurrent channel/product insertion conflicts up to three times. Provider failures, slug conflicts and exhausted retries propagate to the listener/outbox. Rebuild preserves prior committed batches on failure and retries the failed batch from its persisted cursor. No distributed rollback or source aggregate compensation is performed.

## 5. Fresh-start storage

Discovery V1 initializes an empty discovery database with a facts-only product table, a relational indexed variant-membership table, and a persisted rebuild-run table. It enforces channel/product and channel/slug uniqueness and uses local foreign keys only. It does not contain stored prices or currency fields. This implementation rewrites the discovery initial migration; it does not add an upgrade/backfill path and never resets an existing database automatically.
