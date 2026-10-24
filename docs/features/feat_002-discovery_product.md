# DiscoveryProduct
## Storefront Query · Catalog · Inventory · Pricing · Sales Channel

> **Audit status (2026-10-23):** Baseline requirements, not a completed implementation: projector/rebuild use cases are empty and browse/PDP use cases return null, despite existing persistence and HTTP scaffolding.
> **Planned supersession:** [Discovery Wiring & Projection Reconciliation](feat_006-discovery-wiring-projection-reconciliation.md) replaces stored pricing and price-triggered refresh; [Storefront API Cutover & Response Contract Refactoring](feat_007-storefront-api-cutover-contract-refactoring.md) replaces raw/dual channel selectors and channel-currency buyability. These changes take effect together at the verified cutover, not merely by editing this file.

### Problem Statement

Customers need channel-scoped browse and product detail views maintained independently of catalog authoring. This baseline captures the original materialized-price approach; the linked successor features define the multi-region implementation contract.

### Acceptance Criteria

- When a product variant is published or updated, inventory changes, price changes, or sales channel status changes, the system asynchronously projects a channel-scoped `DiscoveryProduct` document for each affected `(salesChannelId, productId)` pair. A variant offer is buyable if and only if the variant is published to the channel, the parent product status is `ACTIVE`, the sales channel is in `ENABLED` status, a price is quoted in the channel currency, and routed inventory is in stock (or inventory tracking is disabled).
- A product document is visible in customer browse queries if and only if the sales channel is `ENABLED`, the product status is `ACTIVE`, and at least one variant is published to that channel. If a channel is disabled, products remain published in catalog state but fail closed and are hidden from storefront browse and search results.
- Projection updates are idempotent and enforce out-of-order event protection. If an incoming integration event has an `occurredAt` timestamp strictly older than the existing projection's `updatedAt`, the update is ignored as stale. Channel-specific events project only for the targeted channel, and SKU-level inventory events deduplicate product-channel pairs before recalculation.
- When a product has no remaining catalog variants, all existing channel projections for that product are unpublished with offers emptied and buyability cleared. When an unpublished or deleted product is queried via direct PDP lookup, the system returns a `404 OfferNotFound` error.
- Customer browse and PDP queries are served from the pre-computed projection via `GET /api/v1/storefront-query/products` (paged collection with price range and stock badges) and `GET /api/v1/storefront-query/products/{slug}` (full product detail with variant offers). A request selects its channel with either `salesChannelId` or the `X-Publishable-Key` header; if both are present, they must resolve to the same channel. Requests without either valid selector fail, and queries against a disabled channel fail with `400 ChannelDisabled`. Key lifecycle and management are defined in [Sales-Channel Key Lifecycle & Authenticated Management](feat_003-sales-channel-key-lifecycle-management.md).
- The system provides an idempotent rebuild mechanism that scans all active publications from Catalog, queries current prices and routed inventory availability across channels, and overwrites drifted or missing projection documents without requiring application downtime or interfering with transactional write domains.

### Related Specifications:

- [Discovery Wiring & Projection Reconciliation](feat_006-discovery-wiring-projection-reconciliation.md)
- [Storefront API Cutover & Response Contract Refactoring](feat_007-storefront-api-cutover-contract-refactoring.md)
- [Browse, Project, and Rebuild workflow](../spec/workflows/browse-and-discover-products-workflow.md)
- [Catalog Product domain spec](../spec/domain/catalog/architecture/product-domain-spec.md)
- [Catalog Product Publication domain spec](../spec/domain/catalog/architecture/product-publication-domain-spec.md)
- [Sales Channel domain spec](../spec/domain/sales-channel/architecture/sales-channel-domain-spec.md)
- [Inventory Item domain spec](../spec/domain/inventory/architecture/inventory-item-domain-spec.md)
- [Pricing domain spec](../spec/domain/pricing/architecture/pricing-domain-spec.md)
- [Storefront Query tech spec](../spec/system/storefront-query-tech-spec.md)
- [Sales-Channel Key Lifecycle & Authenticated Management](feat_003-sales-channel-key-lifecycle-management.md)
