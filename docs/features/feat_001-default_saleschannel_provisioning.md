# DefaultSaleChannel
## Sales Channel · Merchant

### Acceptance Criteria
- Before application readiness, the system provisions one platform-owned `MARKETPLACE` channel named `Marketplace` in `ENABLED` status through the Sales Channel application command path. Startup provisioning retries transient write conflicts up to three times, each attempt using a fresh transaction with 50 ms and 100 ms backoff; startup fails if provisioning remains unsuccessful.
- When a merchant approval is committed, Merchant publishes a versioned `MerchantApprovedIntegrationEvent` containing the merchant identifier and approval occurrence time through delivery of the outbox-backed approval event. Sales Channel consumes it and provisions one merchant-owned `WEBSITE` channel named `Website` in `ENABLED` status.
- Provisioning is idempotent across application restarts, event redelivery, and concurrent execution. Existing channels, including disabled channels, are returned unchanged. Different merchants may each have one Website; a merchant may have at most one Website, and the platform may have at most one Marketplace.
- If Website provisioning fails, the exception propagates so outbox delivery can retry; the committed merchant approval is not reversed. Historical approvals are not backfilled. A failed uniqueness migration requires explicit correction of existing duplicates and does not modify existing channel rows.
- A schema-only migration enforces the Marketplace and per-merchant Website uniqueness rules with partial unique indexes. It creates no channel data and contains no SQL inserts, seed scripts, or database-driven provisioning. POS provisioning, Storefront creation, product publication, and merchant suspension or closure synchronization are outside this feature.

### Related Specifications:
- [Sales Channel domain spec](../domain/sales-channel/architecture/sales-channel-domain-spec.md) 
- [Merchant domain spec](../domain/merchant/architecture/merchant-domain-spec.md) 
- [Transactional Outbox tech spec](../system/transactional-outbox-tech-spec.md)