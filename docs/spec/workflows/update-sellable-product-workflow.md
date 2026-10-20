# Workflow: Update Sellable Product

> **Purpose:** Apply a composed product snapshot, synchronize prices and inventory, and reconcile the product's channel publications.
> **Starts When:** A caller posts to `/api/v1/workflows/update-sellable-product`.
> **Successful Result:** Requested product, listing, price, inventory, and publication operations complete.
> **Participating Domains:** Catalog · Pricing · Inventory · Sales Channel
> **Related Domain Specs:** [Catalog Product](../domain/catalog/architecture/product-domain-spec.md) · [Catalog Product Publication](../domain/catalog/architecture/product-publication-domain-spec.md) · [Pricing](../domain/pricing/architecture/pricing-domain-spec.md) · [Inventory](../domain/inventory/architecture/inventory-item-domain-spec.md) · [Sales Channel](../domain/sales-channel/architecture/sales-channel-domain-spec.md)
> **Sales Channel Reference:** Channel ownership, lifecycle, and usability assertions are defined in the [Sales Channel domain spec](../domain/sales-channel/architecture/sales-channel-domain-spec.md); variant/channel publication records remain owned by Catalog.

## 1. Step-by-Step Execution Path

Steps run in the order below. Conditional steps complete without requests when their input is absent or their publication condition is false.

| # | Step | Owner | Action and completion condition |
|---:|---|---|---|
| 1 | `update-product` | Catalog | Update product metadata and synchronize the requested variant matrix; wait for `SellableProductProductUpdatedEvent`. |
| 2 | `replace-medias` | Catalog | Replace media when supplied; wait for `ProductMediaReplacedEvent`. |
| 3 | `replace-descriptions` | Catalog | Replace descriptions when supplied; wait for `ProductDescriptionsReplacedEvent`. |
| 4 | `apply-status` | Catalog | Apply a nonblank requested status; wait for `ProductStatusAppliedEvent`. |
| 5 | `sync-variant-prices` | Pricing | Fan out one sync per pricing line; skip when no lines are supplied and otherwise wait for every `VariantPriceSyncedEvent`. |
| 6 | `sync-inventory-item` | Inventory | Fan out one sync per inventory line; skip for an empty list and otherwise wait for every `InventoryItemSyncedEvent`. |
| 7 | `assert-channel` | Sales Channel | If publication lines exist, assert each unique channel; wait for all assertions. |
| 8 | `assert-product` | Catalog | If publication lines exist, assert the product; wait for `ProductAssertedEvent`. |
| 9 | `assert-channel-stock-path` | Inventory | If publication lines exist, check each unique channel's inventory route; wait for a result from every channel. |
| 10 | `write-publication` | Catalog | If publication lines exist, publish each requested variant/channel line; wait for all writes. |
| 11 | `unpublish-publication` | Catalog | Unpublish each explicitly supplied unpublish line; wait for all acknowledgements. |

Media, descriptions, and status are optional. Publication validation and writing are conditional on publication lines. Pricing and inventory synchronization support fan-out; lines are correlated by SKU or supplied IDs as validated by the request.

```mermaid
sequenceDiagram
    participant Caller
    participant Workflow as Workflow Coordinator
    participant Catalog
    participant Pricing
    participant Inventory
    participant Channel as Sales Channel
    Caller->>Workflow: Start update request
    Workflow->>Catalog: Update product; replace optional listing data; apply optional status
    Catalog-->>Workflow: Product and listing acknowledgements
    opt Pricing lines provided
        Workflow->>Pricing: Sync prices per line
        Pricing-->>Workflow: Price synced per line
    end
    opt Inventory lines provided
        Workflow->>Inventory: Sync inventory per line
        Inventory-->>Workflow: Inventory synced per line
    end
    opt Publication lines provided
        Workflow->>Channel: Assert each unique channel
        Channel-->>Workflow: ChannelAssertedEvent per channel
        Workflow->>Inventory: Check inventory route per unique channel
        Inventory-->>Workflow: StockPathCheckedEvent per channel
        Workflow->>Catalog: Write publications
        Catalog-->>Workflow: Publication acknowledgements
    end
    opt Unpublish lines provided
        Workflow->>Catalog: Unpublish requested publications
        Catalog-->>Workflow: Unpublication acknowledgements
    end
```

## 2. Events & Signals

| Step | Request event | Completion event |
|---|---|---|
| `update-product` | `RequestUpdateProductSetEvent` | `SellableProductProductUpdatedEvent` |
| `replace-medias` | `RequestReplaceProductMediaEvent` | `ProductMediaReplacedEvent` |
| `replace-descriptions` | `RequestReplaceProductDescriptionsEvent` | `ProductDescriptionsReplacedEvent` |
| `apply-status` | `RequestApplyProductStatusEvent` | `ProductStatusAppliedEvent` |
| `sync-variant-prices` | `RequestSyncVariantPriceEvent` | `VariantPriceSyncedEvent` |
| `sync-inventory-item` | `RequestSyncInventoryItemEvent` | `InventoryItemSyncedEvent` |
| `assert-channel` | `RequestAssertChannelEvent` | `ChannelAssertedEvent` |
| `assert-product` | `RequestAssertProductEvent` | `ProductAssertedEvent` |
| `assert-channel-stock-path` | `RequestCheckChannelStockPathEvent` | `StockPathCheckedEvent` (includes `missingRoute`) |
| `write-publication` | `RequestWritePublicationEvent` | `ProductPublishedToChannelEvent` |
| `unpublish-publication` | `RequestUnpublishProductFromChannelEvent` | `ProductUnpublishedFromChannelEvent` |

Catalog, Pricing, and Inventory step listeners emit `SellableProductStepFailedEvent` when their workflow runners catch an operation exception. Sales Channel assertions and Catalog product/publication operations emit `PublishProductStepFailedEvent` on caught exceptions. These workflow signals are separate from domain and integration events. If an expected completion never arrives, the workflow sweeper times out the waiting step and starts failure recovery without a failure event.

The inventory-route check completes with a `StockPathCheckedEvent` for each unique channel and records `missingRoute` in the workflow checkpoint. A missing route does not itself fail the workflow or suppress publication; the requested publication writes are still attempted.

## 3. Compensation & Failure Recovery

The engine gathers compensation requests from completed steps in reverse definition order and publishes them as a batch. Acknowledgements do not serialize dispatch of later compensation requests.

| Reverse step | Completed action | Compensation request | Acknowledgement |
|---|---|---|---|
| `unpublish-publication` | Removed a publication | `RequestWritePublicationEvent` per completed unpublication | `ProductPublishedToChannelEvent` |
| `write-publication` | Added a publication | `RequestUnpublishProductCompensationEvent` per completed publication | `ProductUnpublishedFromChannelEvent` |
| `sync-variant-prices` | Created a new price set | `RequestDeletePriceSetCompensationEvent` for price sets created in this run | `PriceSetDeletedEvent` |

In-place price edits, Catalog product/variant changes, inventory operations, and earlier listing updates are not restored. Inventory operations may include stock mutations and are intentionally not rolled back. Publication compensation restores/unpublishes only the completed publication actions tracked by this workflow; it does not restore the product snapshot.

The engine may finish `COMPENSATED` after all requested compensations are acknowledged. Failure before a compensable action, or a compensation timeout, can finish `FAILED`; terminal UI status may also indicate partial application. A `FAILED` or `COMPENSATED` status alone does not mean the product snapshot was rolled back.

The terminal `partiallyApplied` flag is calculated from the update context: an acknowledged product update, any recorded price pair or inventory item, or publication changes that remain uncompensated make it true. It is included only for terminal `FAILED` or `COMPENSATED` outcomes. Because the context depends on received signals, an operation committed without its completion signal may not be reflected; the flag does not promise that the product snapshot was restored.

```mermaid
sequenceDiagram
    participant Workflow as Workflow Coordinator
    participant Catalog
    participant Pricing

    Note over Workflow: On a typed failure or waiting-step timeout, collect compensations in reverse definition order
    opt Completed unpublish actions
        Workflow-->>Catalog: RequestWritePublicationEvent
    end
    opt Completed publication writes
        Workflow-->>Catalog: RequestUnpublishProductCompensationEvent
    end
    opt New price sets created
        Workflow-->>Pricing: RequestDeletePriceSetCompensationEvent
    end
    Note over Workflow: Publish the collected requests together; no acknowledgement gates another request
    opt Catalog compensation requested
        Catalog-->>Workflow: Publication compensation acknowledgements
    end
    opt Price-set compensation requested
        Pricing-->>Workflow: PriceSetDeletedEvent
    end
    alt All requested compensations acknowledged
        Workflow->>Workflow: Mark COMPENSATED
    else No compensation requests or compensation timeout
        Workflow->>Workflow: Mark FAILED
    end
```

## 4. Aggregate Cross-Lifecycle Matrix

| Stage | Catalog product | Prices | Inventory | Publications |
|---|---|---|---|---|
| Start | Existing | Existing or absent | Existing or absent | Existing |
| After `update-product` | Requested metadata and variant changes applied | Unchanged | Unchanged | Unchanged |
| After sync steps | Updated | Existing prices changed or new sets created | Requested lines created/changed | Unchanged |
| Completed | Updated | Synchronized when lines supplied | Synchronized when lines supplied | Requested publishes and unpublishes applied |
| Failure/compensation | Catalog changes retained | In-place edits retained; new sets may be deleted | Changes retained | Completed publish/unpublish actions are compensated when possible |

Implementation references: `store/src/main/java/com/grab/store/workflows/internal/workflows/updatesellableproduct/UpdateSellableProductDefinition.java`, `UpdateSellableProductContext.java`, and `store/src/main/java/com/grab/store/catalog/internal/event/PublishProductToChannelCatalogEventListener.java`.
