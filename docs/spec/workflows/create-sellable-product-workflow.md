# Create Sellable Product Workflow

## Status

The app registers definition/payload versions 1 and 2. Version 2 uses typed optional steps, narrow owner request/result records, and a local final projection. Version 1 retains its original graph and routes for pinned executions and queued events. Starts and workers remain disabled by default.

Participant compensations are not implemented. A committed product, price, inventory, or publication mutation cannot currently be rolled back by this workflow; cancellation or a later failure after such a mutation requires attention. Known validation, scope, and participant idempotency conflicts emit non-retryable outcomes after the owner transaction rolls back. Other participant failures rely on outbox redelivery and can become unknown outcomes at the task deadline.

## Contract

| Property | Value |
|---|---|
| Name/version | `create-sellable-product` / 1 or 2 |
| Payload version | 1 for definition 1, 2 for definition 2 |
| Caller scope | Trusted caller scope ID must equal the input merchant ID before execution creation |
| Input | Merchant and actor IDs, product fields, variant types and variants, optional media/descriptions/status, prices, inventory lines, publication targets |
| Result | Product ID, resolved variant IDs/SKUs, price set IDs, inventory item IDs, published targets |
| Batch bound | 100 lines per collection |
| ID encoding | `Id` values serialize as scalar JSON strings and restore through the workflow payload codec |
| Replacement semantics | `null` media/descriptions means omitted; an empty list means clear the field |

## Steps

| Order | Owner/operation | Trigger and behavior | Output | Recovery behavior |
|---|---|---|---|---|
| 1 | Catalog: `catalog.create-product-set` | Always. Creates the product and requested variants in one catalog command. | Product ID and resolved variant IDs/SKUs | No guarded delete compensation is available. |
| 2 | Catalog: `catalog.replace-media` | Runs when media is present, including an explicit empty list. | Acknowledgement in version 2 | No restore compensation is available. |
| 3 | Catalog: `catalog.replace-descriptions` | Runs when descriptions are present, including an explicit empty list. | Acknowledgement in version 2 | No restore compensation is available. |
| 4 | Catalog: `catalog.apply-status` | Runs when a status was supplied. Product creation starts without status so the legacy order is retained. | Acknowledgement in version 2 | No restore compensation is available. |
| 5 | Pricing: `pricing.create-variant-price-sets` | Runs when price lines are present. Resolves every line and submits the bounded batch in one pricing transaction. | Created price set IDs | No price-set delete compensation is available. |
| 6 | Inventory: `inventory.create-items` | Runs when inventory lines are present. Accepts only `CREATE`, validates the full bounded batch, and writes in one inventory transaction. | Inventory item IDs | No cleanup compensation is available. |
| 7 | Sales channel: `sales.assert-publication-targets` | Runs only when requested status is `ACTIVE` and targets are present. Checks channel existence, merchant ownership, and enabled state. | Acknowledgement in version 2 | Read-only. Known assertion failures are non-retryable. |
| 8 | Catalog: `catalog.assert-product-eligibility` | Same publication condition. Checks product existence and active status. | Acknowledgement in version 2 | Read-only. |
| 9 | Inventory: `inventory.assert-stock-paths` | Same publication condition. Checks stock route availability for each target channel using the catalog merchant. | Acknowledgement in version 2 | Read-only. A missing route is a non-retryable failure. |
| 10 | Catalog: `catalog.publish-targets` | Runs after assertions. Resolves all target variants before issuing publication commands. | Targets written by this execution | No unpublish compensation is available. |
| 11 | Workflow: local result map (version 2) or `workflow.result.create-sellable-product` | Always after the selected branches. | Typed workflow result | Pure projection in version 2. |

The static graph uses persisted choices and typed pass-through branches for omitted work. Catalog, pricing, and inventory listeners write a logical-operation receipt and outcome event in the same owner transaction as successful commands. Matching duplicate requests replay the recorded result. Known invalid requests and ownership/idempotency conflicts roll back, then emit a non-retryable outcome in a fresh owner transaction. Other failures roll back and rely on outbox redelivery.

In version 2 the persisted workflow stage remains private to orchestration. The bound task sends its typed owner request and receives a smaller typed result. The orchestration merges that result into its stage and projects the final result locally. See the [typed authoring contracts](../system/workflow-framework-tech-spec.md#5-authoring-typed-definitions).

## Current verification and remaining work

| Scenario | Status |
|---|---|
| Active-status publication selector and missing targets | Passed focused application test |
| Omitted versus empty media/descriptions | Passed focused application test |
| Exact route registration and ID payload encoding | Passed focused application test |
| Owner receipt migrations and uniqueness against PostgreSQL | Passed against PostgreSQL 16 Testcontainers |
| Product, variant, price, and inventory creation through app ports and real owner outboxes | Passed in `SellableProductWorkflowEndToEndTest` against PostgreSQL 16 Testcontainers |
| Concurrent duplicate product-creation dispatches after commit, using distinct transport IDs | Passed; the original owner receipt and completed workflow result remain unchanged |
| Publication assertions and publication mutation | Passed in the parameterized PostgreSQL outbox scenario for both versions |
| Concurrent first-delivery receipt race, process restart during execution, and participant compensation | Not covered |
| Trusted seller-scope authorization and participant compensation | Not implemented |

See [sellable product workflow feature](../features/feat_015-sellable-product-workflows-on-app-runtime.md) and [workflow runtime spec](../system/workflow-headless-runtime-and-protocol-tech-spec.md).
