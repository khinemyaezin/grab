# DiscoveryProduct
## Storefront Query · Catalog · Inventory · Pricing · Sales Channel

### Acceptance Criteria
- When a product variant is published or updated, inventory changes, price changes, or sales channel status changes, the system asynchronously projects a channel-scoped `DiscoveryProduct` document for each affected `(salesChannelId, productId)` pair. A variant offer is buyable if and only if the variant is published to the channel, the parent product status is `ACTIVE`, the sales channel is in `ENABLED` status, a price is quoted in the channel currency, and routed inventory is in stock (or inventory tracking is disabled).
- A product document is visible in customer browse queries if and only if the sales channel is `ENABLED`, the product status is `ACTIVE`, and at least one variant is published to that channel. If a channel is disabled, products remain published in catalog state but fail closed and are hidden from storefront browse and search results.
- Projection updates are idempotent and enforce out-of-order event protection. If an incoming integration event has an `occurredAt` timestamp strictly older than the existing projection's `updatedAt`, the update is ignored as stale. Channel-specific events project only for the targeted channel, and SKU-level inventory events deduplicate product-channel pairs before recalculation.
- When a product has no remaining catalog variants, all existing channel projections for that product are unpublished with offers emptied and buyability cleared. When an unpublished or deleted product is queried via direct PDP lookup, the system returns a `404 OfferNotFound` error.
- Customer browse and PDP queries are served from the pre-computed projection via `GET /api/v1/storefront-query/products?salesChannelId={id}` (paged collection with price range and stock badges) and `GET /api/v1/storefront-query/products/{slug}?salesChannelId={id}` (full product detail with variant offers). Requests without `salesChannelId` fail with `400 SalesChannelRequired`, and queries against a disabled channel fail with `400 ChannelDisabled`.
- The system provides an idempotent rebuild mechanism that scans all active publications from Catalog, queries current prices and routed inventory availability across channels, and overwrites drifted or missing projection documents without requiring application downtime or interfering with transactional write domains.

### Related Specifications:
- [Catalog Product domain spec](../domain/catalog/architecture/product-domain-spec.md)
- [Catalog Product Publication domain spec](../domain/catalog/architecture/product-publication-domain-spec.md)
- [Sales Channel domain spec](../domain/sales-channel/architecture/sales-channel-domain-spec.md)
- [Inventory Item domain spec](../domain/inventory/architecture/inventory-item-domain-spec.md)
- [Pricing domain spec](../domain/pricing/architecture/pricing-domain-spec.md)
- [Storefront Query tech spec](../system/storefront-query-tech-spec.md)
