# Create sellable workflow implementation plan

## Milestone

Make the standalone headless service in `service/workflow` the orchestrator for a new `create-sellable-workflow` definition. The milestone is complete when a caller can start the registered definition through the existing command port, advance it with simulated owner outcomes through the signal port, restart the service at a waiting step, and inspect progress through the query port.

Use the executable `store` definition for business order, branch conditions, and compensation obligations. Two details matter for parity. Every created variant needs a price set. A completed stock-path check that reports a missing route records the channel and allows publication to proceed. [Legacy graph](../store/src/main/java/com/grab/store/workflows/internal/workflows/createsellableproduct/CreateSellableProductDefinition.java) · [Legacy context and branch conditions](../store/src/main/java/com/grab/store/workflows/internal/workflows/createsellableproduct/CreateSellableProductContext.java) · [Legacy behavior tests](../store/src/test/java/com/grab/store/workflows/createsellableproduct/CreateSellableProductDefinitionTest.java)

## 1. Establish the hexagonal boundary

| Responsibility | Implementation decision |
|---|---|
| Workflow application | Create `com.grab.service.workflow.internal.application.definition.createsellable` inside the existing `workflow-service` Maven module. Place the provider, immutable input, private stage, result, and typed operation bindings here. Limit dependencies to Java and the framework ID and workflow APIs. |
| Composition | Construct the provider in Spring configuration. Change the currently empty registry factory to register each `WorkflowDefinitionProvider` using `register(provider)`. Duplicate identities must fail during startup. |
| Inbound adapters | Reuse the command, signal, and query ports, their message adapters, the workflow-scoped buses, and their transactional handlers. Preserve the published Java/RMI signatures. |
| Outbound adapters | Use the existing engine ports and `workflow-infrastructure` implementations for executions, receipts, timers, history, and outbox dispatch. Workflow-specific code dispatches typed owner requests. Each owner remains responsible for its own business data. |
| Dependency enforcement | Add an ArchUnit rule for the definition package. Prohibit dependencies on Spring, JPA, Jackson, service adapters, `app`, `store`, and other service internals. Reuse the existing headless and scoped-bus checks. |

The framework already provides the typed graph DSL and registry. The standalone service already wires the durable runtime and its persistence adapters. [Registry wiring](../service/workflow/workflow-service/src/main/java/com/grab/service/workflow/internal/config/WorkflowConfiguration.java) · [Workflow DSL](../framework/src/main/java/com/grab/framework/workflow/WorkflowDsl.java)

## 2. Define identity, start validation, and payloads

1. Register identity `create-sellable-workflow`, definition version **1**, payload version **1**. The distinct name gives new executions their own history and idempotency contract. Existing `create-sellable-product` executions stay on their original runtime.
2. In the existing start use case, resolve the definition and payload codec, decode the typed input, then invoke registry start validation before creating an execution, receipt, or dispatch. Require the trusted caller scope to match the input merchant ID.
3. Represent IDs with framework `Id` and serialize them as scalar JSON strings. Carry the merchant, actor, inventory scope, product, variants and options, pricing, inventory lines, media, descriptions, and publication targets. Preserve omitted media/descriptions separately from explicit empty lists.
4. Apply the store request rules. Require pricing coverage for every requested variant SKU. Require inventory lines for managed variants and reject inventory lines for unmanaged variants. Correlate returned variant IDs to SKUs before preparing downstream requests. [Pricing validation](../store/src/main/java/com/grab/store/workflows/internal/workflows/createsellableproduct/rest/dto/request/PricingCoverageValidator.java) · [Inventory validation](../store/src/main/java/com/grab/store/workflows/internal/workflows/createsellableproduct/rest/dto/request/InventoryTrackingValidator.java)
5. Register the definition even when new starts are disabled. Add a definition-specific start gate that defaults to disabled. Apply it only to new starts so persisted executions can still receive signals after restart. The standalone service’s general start and worker flags currently default to enabled. [Start use case](../service/workflow/workflow-service/src/main/java/com/grab/service/workflow/internal/service/StartWorkflowService.java) · [Runtime settings](../service/workflow/workflow-service/src/main/resources/application.yml)

## 3. Build the graph in store order

Use `WorkflowDsl.flow` for the sequence, `bind` for typed owner tasks, and `when` for persisted choices. For collection operations, bind one batch request and outcome. Before advancing, match the returned keys against the requested work so a partial acknowledgement cannot complete a step. Keep the orchestration stage private and send each owner only its operation payload.

| Order | Stable step ID | Owner operation | Completion rule |
|---|---|---|---|
| 1 | `create-product` | `catalog.create-product-set` | Create the product and requested variants. Capture product ID, effective status, and SKU-to-variant IDs. |
| 2 | `replace-medias` | `catalog.replace-media` | Skip omitted media. Send an explicit empty list to clear media and wait for acknowledgement. |
| 3 | `replace-descriptions` | `catalog.replace-descriptions` | Skip omitted descriptions. Send an explicit empty list to clear descriptions and wait for acknowledgement. |
| 4 | `apply-status` | `catalog.apply-status` | Run for a supplied, nonblank status. Save the effective status acknowledged by Catalog. |
| 5 | `create-variant-prices` | `pricing.create-variant-price-sets` | Resolve a price line for each created variant. Capture one price-set ID per variant. |
| 6 | `create-inventory-item` | `inventory.create-items` | Skip empty inventory. Otherwise resolve each requested SKU and capture an inventory item ID for each SKU/location pair. |
| 7 | `assert-channel` | `sales.assert-publication-targets` | Enter the publication branch only when the saved effective status is `ACTIVE` and targets exist. Check each distinct target channel. |
| 8 | `assert-product` | `catalog.assert-product-eligibility` | Wait for Catalog to confirm eligibility of the created product. |
| 9 | `assert-channel-stock-path` | `inventory.assert-stock-paths` | Wait for a check result for every distinct channel. Save missing-route channel IDs and continue when all channels have answered. |
| 10 | `write-publication` | `catalog.publish-targets` | Resolve target SKUs to variant IDs and capture the variant/channel pairs actually written. |

Group steps 7–10 under one persisted choice. Finish either branch with a local `map` that returns the product ID, resolved variants, price-set IDs, inventory item IDs, published pairs, and missing-route findings.

## 4. Define recovery and compensation

| Successful effect | Planned recovery contract |
|---|---|
| Product creation | Request guarded deletion of the product identified by the stored creation outcome and merchant scope. |
| Price-set creation | Request deletion of the price-set IDs recorded by this execution. |
| Publication writes | Request unpublication of the recorded variant/channel pairs written by this execution. |
| Media replacement, description replacement, status application, inventory creation | Mark each task as mutating. The engine reports `NEEDS_ATTENTION` on a later failure once any of these uncompensated effects has succeeded. |
| Channel, product, and stock-path assertions | Mark each task read-only. A completed assertion creates no compensation obligation. |

Use the engine’s persisted forward input and output to construct compensation requests. Correlate acknowledgements by execution, logical operation, attempt, and phase. Verify that a retry or duplicate acknowledgement cannot advance the graph twice. [Compensation policy](../framework/src/main/java/com/grab/framework/workflow/internal/CompensationPolicy.java)

## 5. Verify before enabling starts

| Test | Acceptance criteria |
|---|---|
| Registration and architecture | The service starts headless with one registered provider. Duplicate registration fails. The definition passes dependency checks and registers with its start gate disabled. |
| Start contract | Wrong merchant scope, unsupported versions, invalid pricing coverage, and invalid inventory tracking produce zero execution, start-receipt, and outbox rows. A repeated valid start with the same scope, key, and input returns the original execution. |
| Graph parity | Test every step in order, multiple variants and locations, omitted versus empty replacements, publication skipped for inactive status or absent targets, effective status from Catalog, and publication after a reported missing stock path. Missing batch results cannot advance the execution. |
| Port-to-outbox integration | With PostgreSQL Testcontainers, start through the command port, inspect persisted dispatch payloads and scalar IDs, submit simulated owner outcomes through the signal port, then verify terminal result, node state, and query-port history. |
| Restart and duplicate delivery | Stop at a waiting owner task and restart on the same database. Acknowledge the original dispatch twice. Verify the stored branch, one logical progression, and one downstream dispatch. |
| Failure recovery | Fail after pricing on a path with only compensable writes. Verify price-set and product compensation requests, their target IDs, and acknowledgement-driven completion. Separately fail after inventory creation and assert `NEEDS_ATTENTION`. |
| Reactor verification | Run the workflow-service tests and dependencies with `mvn -pl service/workflow/workflow-service -am test` using Docker for PostgreSQL tests. Record the results before enabling starts in an integration environment. |

Production adoption requires owner-side adapters and idempotent receipts, cross-process outbox dispatch and outcome delivery, and a multi-service acceptance run. Keep this definition’s production start gate disabled until those components are verified together.
