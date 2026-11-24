# Update Product Variant Workflow

## Status

The app registers definition/payload versions 1 and 2. Version 2 uses typed optional steps, narrow owner request/result records, and a local final projection. Version 1 retains its original graph and routes for pinned executions and queued events. Starts and workers remain disabled by default.

This definition does not yet declare participant compensation. If cancellation or a later step failure follows a successful mutation, the execution requires attention rather than claiming rollback.

## Contract

| Property | Value |
|---|---|
| Name/version | `update-product-variant` / 1 or 2 |
| Payload version | 1 for definition 1, 2 for definition 2 |
| Caller scope | Trusted caller scope ID must equal the input merchant ID before execution creation |
| Input | Merchant and actor IDs, product and variant IDs, optional SKU and inventory policy, pricing sync intent and lines, inventory sync intent and lines |
| Result | Product and variant IDs, resolved SKU, price set IDs, inventory item IDs |
| Batch bound | 100 lines per collection |
| ID encoding | `Id` values serialize as scalar JSON strings and restore through the workflow payload codec |

## Steps

| Order | Owner/operation | Trigger and behavior | Output | Recovery behavior |
|---|---|---|---|---|
| 1 | Catalog: `catalog.update-variant` | Always. Updates the requested variant through the merchandising command bus and returns the resolved SKU. | Updated variant stage | A failure rolls back this owner transaction. No compensation is available after a committed update. |
| 2 | Pricing: `pricing.sync-variant-prices` | Runs only when pricing sync is requested and price lines are present. Validates the bounded batch, resolves IDs/SKUs, then issues owner commands in one pricing transaction. | Affected price set IDs | A failure rolls back the pricing batch. No compensation is available after a committed update/create. |
| 3 | Inventory: `inventory.apply-operations` | Runs only when inventory sync is requested and operations are present. Supports `CREATE`, `ADJUST`, `DAMAGE`, `WRITE_OFF`, and `REORDER`; validates all lines before commands. Requires `merchant.account` scope and matching merchant ID. | Affected inventory item IDs | A failure rolls back the owner batch. Inventory changes have no compensation route. |
| 4 | Workflow: local result map (version 2) or `workflow.result.update-product-variant` | Always, after prior optional work. | Typed workflow result | Pure projection in version 2. |

Skipped pricing and inventory branches pass the immutable stage through unchanged. Retry attempts preserve their logical operation identity and owner receipts replay a previously committed successful result for the same request fingerprint. Known invalid inputs, scope mismatches, and receipt conflicts return non-retryable outcomes after rollback; other failures rely on outbox redelivery.

In version 2 the persisted workflow stage remains private to orchestration. The bound task sends its typed owner request and receives a smaller typed result. The orchestration merges that result into its stage and projects the final result locally. See the [typed authoring contracts](../system/workflow-framework-tech-spec.md#5-authoring-typed-definitions).

## Current verification and remaining work

| Scenario | Status |
|---|---|
| Definition registration, route coverage, and optional sync choice | Passed focused application test |
| `Id` scalar codec round-trip | Passed focused application test |
| Owner receipt migrations and uniqueness constraints against PostgreSQL | Passed against PostgreSQL 16 Testcontainers |
| Variant update, price update, and inventory adjustment through real app participants/outboxes | Passed in `SellableProductWorkflowEndToEndTest` against PostgreSQL 16 Testcontainers |
| Duplicate delivery after participant commit, restart recovery during this workflow, and compensation | Not covered |
| Trusted seller-scope authorization at an authenticated ingress | Not implemented; no start REST route is included |
| Known validation/scope/idempotency failure outcomes | Implemented as non-retryable; transient failure classification and compensation acknowledgement are not implemented |

See [sellable product workflow feature](../features/feat_015-sellable-product-workflows-on-app-runtime.md) for overall adoption requirements and [workflow runtime spec](../system/workflow-headless-runtime-and-protocol-tech-spec.md) for runtime semantics.
