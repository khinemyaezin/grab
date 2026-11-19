# Sellable Product Workflows on the Application Runtime

## Status

Application definitions and forward participant routes are implemented for version 1 of all three workflows. `SellableProductWorkflowEndToEndTest` starts each flow through the app Java command port and completes catalog, pricing, and inventory work by polling the real workflow and participant outboxes against PostgreSQL 16. The create flow creates a product, variant, price, and inventory item; the variant update changes price and inventory; the sellable product update changes price and inventory. Production starts and workers remain disabled by default.

The implementation is not ready for production cutover. Participant compensation routes are not implemented. Known invalid requests, owner-scope mismatches, and participant idempotency conflicts emit non-retryable outcomes after the owner transaction rolls back; other failures rely on outbox redelivery and can become unknown outcomes at the workflow deadline. A failure after a committed mutation remains an incident requiring reconciliation; the runtime must not report it as rolled back.

## Delivered behavior

| Area | Implemented behavior |
|---|---|
| Definitions | `update-product-variant`, `create-sellable-product`, and `update-sellable-product`, each definition version 1 and payload version 1, are registered by the headless workflow group in `app`. |
| Graph | Typed sequences and persisted choices preserve the legacy operation order. Pass-through branches preserve stage type and represent omitted work. |
| Payloads | Immutable records use `Id`; the workflow Jackson codec encodes IDs as scalar strings. Omitted media/descriptions remain `null`; empty lists request clearing. Batches are limited to 100 lines. |
| Dispatch | Catalog, pricing, inventory, and workflow-local listeners declare exact definition, definition version, payload version, operation, and phase routes. Startup fails if a task has no exact route. |
| Owner execution | Catalog, pricing, and inventory listeners use their group command/query buses and group transaction managers. Owner-local receipt, business writes, and outcome outbox records share the listener transaction. Duplicate logical operations replay the saved successful result if the request fingerprint matches. |
| Merchant scope | Before creating an execution, the start service requires the input merchant ID to match the caller scope ID. Owner listeners check that dispatch scope matches the stage merchant ID, and owner commands revalidate merchant/resource ownership in their own schema. A future authenticated ingress must derive this scope from verified seller authorization; these checks do not authenticate a caller by themselves. |
| Inventory scope | Inventory operations require the canonical `merchant.account` scope key and merchant ID; the inventory domain policy rechecks location ownership. |
| Assertions | Sales-channel ownership and publishability checks run in the channel group; stock-path checks run in inventory; product eligibility runs in catalog. Assertion tasks are read-only and create no compensation obligation. |
| Defaults | Start and timer worker flags remain disabled unless explicitly enabled. `store` remains a read-only legacy behavior reference. |

## Known gaps and limits

- Trusted seller authorization must be resolved by a future authenticated ingress before calling the Java start port. The current framework-only migration adds no HTTP route and cannot authenticate a caller-provided `callerScope`; it only rejects a mismatch between that scope ID and the payload merchant ID.
- Only known validation, scope, and logical-operation conflict failures are emitted as non-retryable outcomes after rollback. Other participant failures roll back and rely on outbox redelivery; a task deadline can therefore become an unknown outcome and incident.
- Forward operations have no participant compensation routes yet. Cancellation or a later failure after a successful mutating task is conservatively surfaced as `NEEDS_ATTENTION`.
- A 100-line default is enforced at the payload boundary and owner batch handlers; configurability and production workload tuning remain open.
- PostgreSQL owner receipt migrations/uniqueness and generic runtime restart recovery pass with Testcontainers. The end-to-end test also concurrently redelivers the completed catalog product-creation operation under distinct transport event IDs and verifies that the saved participant receipt is reused without changing the workflow result. It does not simulate a process crash between commit and acknowledgement or restart recovery for these product flows; concurrent first deliveries racing before a receipt exists are also not covered.
- The end-to-end test omits publication targets, so it does not exercise the sales-channel ownership assertion, catalog publication assertion, inventory stock-path assertion, or publication mutations.
- Seller HTTP routes and authentication wiring, terminal UI notifications, staging smoke tests, production compensation routes, and cutover remain separate work.

## Verification traceability

| Scenario | Evidence | Status |
|---|---|---|
| Three definitions register; missing or wrong-version routes fail startup | `SellableProductWorkflowDefinitionsTest` | Passed |
| Optional media/descriptions distinguish omitted and empty replacements | `SellableProductWorkflowDefinitionsTest` | Passed |
| Publication selector requires active status and targets | `SellableProductWorkflowDefinitionsTest` | Passed |
| ID scalar encoding and record payload round-trip | `SellableProductWorkflowDefinitionsTest` | Passed |
| Full app tests, including architecture checks | `mvn -q -f app/pom.xml test` using an isolated temporary Maven cache | Passed |
| Root app reactor build | `mvn -q -pl app -am test` | Blocked by the pending `service/pom.xml` reference to missing `service-rmi-infrastructure`; left unchanged as unrelated working-tree state |
| Owner receipt migrations and uniqueness constraints | `SellableProductParticipantMigrationTest` | Passed against PostgreSQL 16 Testcontainers |
| Generic workflow runtime start, signals, timer recovery, concurrent receipts, and rollback | `WorkflowRuntimePostgresTest` | Passed against PostgreSQL 16 Testcontainers |
| All three product flows through app start/query ports and real catalog, pricing, inventory, and workflow outboxes | `SellableProductWorkflowEndToEndTest` | Passed against PostgreSQL 16 Testcontainers; publication-target path is not exercised |
| Concurrent duplicate catalog delivery after commit under distinct transport IDs | `SellableProductWorkflowEndToEndTest` | Passed; verifies unchanged receipt and completed execution |
| Concurrent first-delivery receipt race, process restart during product flow, and compensation acknowledgement | PostgreSQL integration coverage | Not covered |

## Attached specifications

- [Create sellable product workflow](../workflows/create-sellable-product-workflow.md)
- [Update sellable product workflow](../workflows/update-sellable-product-workflow.md)
- [Update product variant workflow](../workflows/update-product-variant-workflow.md)
- [Workflow runtime and protocol](../system/workflow-headless-runtime-and-protocol-tech-spec.md)

## Adoption order

### Staging smoke and cutover

Do not enable starts in a shared staging environment until a trusted ingress derives `callerScope` from authenticated seller identity. The Java runtime port is not an authorization boundary.

1. Deploy with `workflow.starts-enabled=false` and `workflow.workers-enabled=false`; verify all three version-1 definitions and exact task routes register.
2. Apply owner receipt migrations in a disposable or staging database; verify catalog V7, pricing V4, and inventory V5 are applied without resetting existing data.
3. Enable starts only for an authenticated staging harness. Run one create, one variant update, and one sellable product update using bounded batches. Verify execution history, owner receipts, business rows, and outcome outbox records in their owning schemas.
4. Redeliver each participant request and confirm the saved result is returned with no second business effect. Exercise a known validation failure and verify it is non-retryable; test unresolved delivery only in isolated data and confirm it becomes an incident.
5. If testing publication, configure a merchant-owned enabled channel, an active product, and a valid inventory stock path; confirm the three read-only assertions precede publication. The current end-to-end test does not cover this path.
6. Keep starts/workers disabled after the smoke run until participant compensation or an explicit operator recovery policy, restart/duplicate-delivery tests, monitoring, and rollback procedures are accepted.
7. For cutover, route only new authenticated starts to `app`; do not migrate active legacy execution state. Existing executions finish on their original runtime. Keep legacy HTTP routes and terminal UI notifications as a separate cutover.
