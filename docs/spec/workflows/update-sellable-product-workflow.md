# Update Sellable Product Workflow

## Status

The app registers definition/payload versions 1 and 2. Version 2 uses typed optional steps, narrow owner request/result records, and a local final projection. Version 1 retains its original graph and routes for pinned executions and queued events. Starts and workers remain disabled by default.

No participant compensation routes are implemented. A successful product, variant, price, inventory, publish, or unpublish mutation remains an effect requiring operator reconciliation if cancellation or a later failure requests rollback. Known invalid requests, scope mismatches, and participant idempotency conflicts emit non-retryable outcomes after owner rollback; other failures rely on outbox redelivery and may become unknown at the task deadline.

## Contract

| Property | Value |
|---|---|
| Name/version | `update-sellable-product` / 1 or 2 |
| Payload version | 1 for definition 1, 2 for definition 2 |
| Caller scope | Trusted caller scope ID must equal the input merchant ID before execution creation |
| Input | Merchant and actor IDs, product fields, variant sync intent/types/variants, optional media/descriptions/status, price and inventory sync intent/lines, publication and unpublication targets |
| Result | Product ID, resolved variant IDs/SKUs, affected price set IDs, inventory item IDs, published and unpublished targets |
| Batch bound | 100 lines per collection |
| ID encoding | `Id` values serialize as scalar JSON strings and restore through the workflow payload codec |
| Replacement semantics | `null` media/descriptions means unchanged; an empty list means clear |

## Steps

| Order | Owner/operation | Trigger and behavior | Output | Recovery behavior |
|---|---|---|---|---|
| 1 | Catalog: `catalog.update-product-set` | Always. Applies product and variant data with the declared `LEAVE_AS_IS`, `FULL_SYNC`, or `COLLAPSE_TO_STANDALONE` intent. | Resolved variants | No guarded restore is available. |
| 2 | Catalog: `catalog.replace-media` | Runs when media is present, including empty replacement. | Updated stage | No restore compensation is available. |
| 3 | Catalog: `catalog.replace-descriptions` | Runs when descriptions are present, including empty replacement. | Updated stage | No restore compensation is available. |
| 4 | Catalog: `catalog.apply-status` | Runs when a status was supplied. | Updated stage | No restore compensation is available. |
| 5 | Pricing: `pricing.sync-product-prices` | Runs when pricing sync is requested and price lines are present. Resolves lines to updated variants before commands. | Affected price set IDs | No guarded price restoration is available. |
| 6 | Inventory: `inventory.sync-product-items` | Runs when inventory sync is requested and operations are present. Supports `CREATE`, `ADJUST`, `DAMAGE`, `WRITE_OFF`, and `REORDER`; validates the batch before writes and requires the canonical `merchant.account` scope. | Affected inventory item IDs | No inventory compensation is available. |
| 7 | Sales channel: `sales.assert-publication-targets` | Runs when publish or unpublish targets exist. Checks channel ownership; enabled state is required for publishing. | Validated stage | Read-only. Known assertion failures are non-retryable. |
| 8 | Catalog: `catalog.assert-product-eligibility` | Same publication-change condition. Product must exist; active status is required when publishing. | Validated stage | Read-only. |
| 9 | Inventory: `inventory.assert-stock-paths` | Same condition; checks stock paths for publish targets. | Validated stage | Read-only. Missing routes are non-retryable failures. |
| 10 | Catalog: `catalog.publish-targets` | Runs first when publish targets are present, after all three assertions. Resolves the whole target batch before writes. | Targets written by this execution | No unpublish compensation is available. |
| 11 | Catalog: `catalog.unpublish-targets` | Runs after publication when unpublish targets are present. Resolves the whole target batch before writes. | Targets removed by this execution | No guarded restoration is available. |
| 12 | Workflow: local result map (version 2) or `workflow.result.update-sellable-product` | Always after selected branches. | Typed workflow result | Pure projection in version 2. |

Choice decisions are stored by the engine and skipped branches pass the stage through. Catalog, pricing, and inventory execute in their own schema transactions. Each owner stores a successful logical-operation receipt and emits its outcome through its local outbox in that transaction. Known validation, scope, and idempotency failures are classified as non-retryable after the failed owner transaction rolls back; other errors are retried by outbox delivery and the workflow deadline can surface unknown outcomes.

In version 2 the persisted workflow stage remains private to orchestration. The bound task sends its typed owner request and receives a smaller typed result. The orchestration merges that result into its stage and projects the final result locally. See the [typed authoring contracts](../system/workflow-framework-tech-spec.md#5-authoring-typed-definitions).

## Current verification and remaining work

| Scenario | Status |
|---|---|
| Route registration, selector behavior, payload ID encoding | Passed focused application tests |
| Omitted versus empty replacements | Passed focused application test |
| PostgreSQL receipt migrations and uniqueness | Passed against PostgreSQL 16 Testcontainers |
| Product/variant, price, and inventory updates through real app participants/outboxes | Passed in `SellableProductWorkflowEndToEndTest` against PostgreSQL 16 Testcontainers |
| Publication assertions and publish/unpublish mutations | Passed in the parameterized PostgreSQL outbox scenario for both versions |
| Duplicate participant delivery after commit, restart recovery during this workflow, and compensation | Not covered |
| Authenticated seller-scope resolution and participant compensation | Not implemented; transient failure classification remains incomplete |

See [sellable product workflow feature](../features/feat_015-sellable-product-workflows-on-app-runtime.md) and [workflow runtime spec](../system/workflow-headless-runtime-and-protocol-tech-spec.md).
