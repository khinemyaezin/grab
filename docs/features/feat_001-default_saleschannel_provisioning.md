# DefaultSaleChannel
## Sales Channel · Merchant

> **Audit status (2026-10-23):** Partially implemented; command provisioning, approval consumption, retry declarations and uniqueness indexes exist. Startup currently runs on ApplicationReadyEvent and creates a Marketplace named `Default Sale Channel`, so the timing/name criteria below are not yet met.
> **Completion workflow:** [Default Sales Channel Provisioning](../spec/workflows/default-sales-channel-provisioning-workflow.md).
> **Regional extension:** [Edge Shopping-Context Resolution](feat_004-edge-shopping-context-resolution.md) coordinates served/default region configuration; this feature alone does not implement contextual discovery.

### Problem Statement

Default selling destinations must be provisioned reliably through application commands, preserving merchant approval when dependent provisioning fails. Database seeds cannot enforce this business workflow across startup retries and approval redelivery.

### Acceptance Criteria

- Before application readiness, the system provisions one platform-owned `MARKETPLACE` channel named `Marketplace` in `ENABLED` status through the Sales Channel application command path. Startup provisioning retries transient write conflicts up to three times, each attempt using a fresh transaction with 50 ms and 100 ms backoff; startup fails if provisioning remains unsuccessful.
- When a merchant approval is committed, Merchant publishes a versioned `MerchantApprovedIntegrationEvent` containing the merchant identifier and approval occurrence time through delivery of the outbox-backed approval event. Sales Channel consumes it and provisions one merchant-owned `WEBSITE` channel named `Website` in `ENABLED` status.
- Provisioning is idempotent across application restarts, event redelivery, and concurrent execution. Existing channels, including disabled channels, are returned unchanged. Different merchants may each have one Website; a merchant may have at most one Website, and the platform may have at most one Marketplace.
- If Website provisioning fails, the exception propagates so outbox delivery can retry; the committed merchant approval is not reversed. Historical approvals are not backfilled. A failed uniqueness migration requires explicit correction of existing duplicates and does not modify existing channel rows.
- A schema-only migration enforces the Marketplace and per-merchant Website uniqueness rules with partial unique indexes. It creates no channel data and contains no SQL inserts, seed scripts, or database-driven provisioning. POS provisioning, Storefront creation, product publication, and merchant suspension or closure synchronization are outside this feature.

### Acceptance Traceability

| Criterion, in order | Completion contract |
| :--- | :--- |
| Platform startup provisioning and retry | Workflow P-01–P-02. |
| Approval-driven Website provisioning | Workflow P-03–P-05. |
| Idempotency and uniqueness | Workflow section 3 and Sales Channel uniqueness constraints. |
| Failure handling and historical scope | Workflow section 3. |
| Schema-only migration and exclusions | Workflow sections 3 and 5. |

### Related Specifications:

- [Default Sales Channel Provisioning workflow](../spec/workflows/default-sales-channel-provisioning-workflow.md)
- [Edge Shopping-Context Resolution](feat_004-edge-shopping-context-resolution.md)
- [Sales Channel domain spec](../spec/domain/sales-channel/architecture/sales-channel-domain-spec.md)
- [Merchant domain spec](../spec/domain/merchant/architecture/merchant-domain-spec.md)
- [Transactional Outbox tech spec](../spec/system/transactional-outbox-tech-spec.md)
