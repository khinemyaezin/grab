# Workflow: Update Product Variant

> **Purpose:** Update a single merchant product variant end-to-end: update the catalog SKU, upsert the variant price if provided, and create or sync inventory lines.
> **Starts When:** API call `POST /api/v1/workflows/update-product-variant` is received.
> **Successful Result:** Catalog variant is updated, the price set is created or updated when a price is provided, and inventory lines are created or synced.
> **Participating Domains:** Catalog · Pricing · Inventory
> **Related Domain Specs:**
> - [Catalog Domain Spec](../domain/catalog/architecture/product-domain-spec.md)
> - [Pricing Domain Spec](../domain/pricing/architecture/pricing-domain-spec.md)
> - [Inventory Domain Spec](../domain/inventory/architecture/inventory-item-domain-spec.md)

---

## 1. Step-by-Step Execution Path

### Normal Path (Happy Path)

| # | Step Name | Owner Domain | Business Action | Starts When | Complete When |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `update-variant` | Catalog | Updates the variant SKU. | Workflow initiated | `VariantUpdatedEvent` received |
| **2** | `sync-variant-prices` | Pricing | Upserts the price for the variant (creates the price set if missing). | Step 1 completes | `VariantPriceSyncedEvent` received |
| **3** | `sync-inventory-item` | Inventory | Executes stock adjustment, damage, write-off, or reorder operations per inventory line. | Step 2 completes | `InventoryItemSyncedEvent` received for every line → Workflow completes |

### Special Flows & Edge Conditions

- **Parallel fan-out:** Step 3 fans out one inventory sync request per inventory line and advances when all sync.
- **Conditional / skipped steps:** Step 2 (pricing) is skipped if no price is provided in the request. Step 3 (inventory) is skipped if no inventory lines are provided.

### Happy Path Flow

```mermaid
sequenceDiagram
    participant Caller
    participant Workflow as Workflow Coordinator
    participant Catalog
    participant Pricing
    participant Inventory

    Caller->>Workflow: Start workflow request
    Workflow->>Catalog: RequestUpdateVariantEvent
    Catalog-->>Workflow: VariantUpdatedEvent
    opt [Price Present]
        Workflow->>Pricing: RequestSyncVariantPriceEvent
        Pricing-->>Workflow: VariantPriceSyncedEvent
    end
    opt [Inventory Lines Present]
        Workflow->>Inventory: RequestSyncInventoryItemEvent (fan-out per line)
        Inventory-->>Workflow: InventoryItemSyncedEvent
    end
    Workflow-->>Caller: Workflow completed notification (terminal UI event)
```

---

## 2. Events & Signals

### Request Events (Workflow → Domain)

| Event Name | Step | Dispatched To | Intent / Payload Summary |
| :--- | :--- | :--- | :--- |
| `RequestUpdateVariantEvent` | `update-variant` | Catalog | Requests updating the variant SKU. |
| `RequestSyncVariantPriceEvent` | `sync-variant-prices` | Pricing | Requests price synchronization (upsert) for the variant. |
| `RequestSyncInventoryItemEvent` | `sync-inventory-item` | Inventory | Requests inventory creation, stock adjustment, damage, write-off, or reorder. |

### Completion Events (Domain → Workflow)

| Event Name | Step | Emitted By | Meaning to Workflow |
| :--- | :--- | :--- | :--- |
| `VariantUpdatedEvent` | `update-variant` | Catalog | Catalog variant updated successfully; advance to price sync. |
| `VariantPriceSyncedEvent` | `sync-variant-prices` | Pricing | Price synchronized for the variant; advance to inventory sync. |
| `InventoryItemSyncedEvent` | `sync-inventory-item` | Inventory | Inventory operation completed for a line; step completes when all lines sync. |

### Failure Events (Domain → Workflow)

| Event Name | Emitted By | Failure Cause | Resulting Workflow Action |
| :--- | :--- | :--- | :--- |
| `SellableProductStepFailedEvent` | Any participant | Validation error, invariant breach, or timeout | Halts forward execution and initiates compensation. |

---

## 3. Compensation & Failure Recovery

> **Starts When:** A step reports failure via `SellableProductStepFailedEvent` after one or more prior steps have already succeeded.

### Compensation Order

| Order | Completed Action | Undo Action | Request Event | Acknowledgement Event |
| :--- | :--- | :--- | :--- | :--- |
| **1** | Step 2: Create a new price set | Delete the price set that was *newly created* during this workflow run | `RequestDeletePriceSetCompensationEvent` | `PriceSetDeletedEvent` |

### Explicitly Non-Compensated Actions

- **Catalog variant SKU:** The variant is intentionally retained to prevent merchant data loss on transient failures, and prior SKUs are not snapshotted for rollback.
- **In-place price updates:** Prior price amounts are not snapshotted for rollback. Only a *newly created* price set is deleted.
- **Inventory items and adjustments:** Stock adjustments, damage reports, write-offs, and newly created items are explicitly not rolled back by business policy.

### Failure Flow

```mermaid
sequenceDiagram
    participant StepOwner as Domain
    participant Workflow as Workflow Coordinator
    participant Pricing

    StepOwner-->>Workflow: SellableProductStepFailedEvent
    Note over Workflow: Halt forward execution
    opt [Newly Created Price Set Present]
        Workflow->>Pricing: RequestDeletePriceSetCompensationEvent
        Pricing-->>Workflow: PriceSetDeletedEvent
    end
    Note over Workflow: Mark workflow COMPENSATED (Partial Rollback)
```

---

## 4. Aggregate Cross-Lifecycle Matrix

| Workflow Stage | Catalog Variant | Pricing PriceSet | InventoryItem |
| :--- | :--- | :--- | :--- |
| **Initial Start** | `ACTIVE` | `ACTIVE` / Non-existent | `INITIALIZED` / `ACTIVE` |
| **After Step 1** | `UPDATED` | `ACTIVE` / Non-existent | `INITIALIZED` / `ACTIVE` |
| **After Step 2** | `UPDATED` | `UPDATED` / `CREATED` | `INITIALIZED` / `ACTIVE` |
| **Workflow Completed** | `UPDATED` | `UPDATED` / `CREATED` | `UPDATED` / `CREATED` |
| **If Compensated** | Retained (Update) | Retained (Update) / Deleted (New) | Retained (Update) |
