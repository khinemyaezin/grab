# ADR-002: Update Sellable Product Workflow

## Status
Accepted

## Context

Merchants edit a sellable product as one form. The UI loads catalog, pricing, and inventory from each bounded context and shows them together. After save, those writes still cross Catalog, Pricing, and Inventory and need one durable process owner for sequence and compensation.

`create-sellable-product` already owns the create path. Catalog already has in-module `UpdateProductCommand`; pricing and inventory already have in-place update commands. A second Process Manager is required so the composed save does not call those APIs independently and leave a half-updated product.

Create-style compensation (delete the product) is wrong on update: it would destroy merchant data.

## Decision

Add sibling workflow `update-sellable-product` as a Process Manager (ADR-006 / ADR-007):

- Client `POST /api/v1/workflows/update-sellable-product` with `productId` plus a full snapshot (product + `variantSync`, pricing lines, inventory lines) and optional `idempotencyKey`. Returns `202` + workflow id. Read composition stays on the client (no BFF GET).
- Orchestrator communicates only via `workflows::events`. Modules dispatch local commands from listeners.
- Reuse `UpdateProductCommand`, `UpdateVariantPriceCommand`, and inventory commands selected by inventory line `op`: `CreateInventoryCommand`, `AdjustStockCommand`, `MarkDamagedCommand`, `WriteOffStockCommand`, `UpdateReorderConfigCommand`.
- Pricing lines carry `sku`, optional `variantId`, and money fields — not `priceSetId`/`priceId`. After catalog update, the orchestrator assigns `variantId` onto each pricing line by matching `SellableProductProductUpdatedEvent.variants` on sku (catalog is the authority, including new SKUs). Pricing then dispatches `UpdateVariantPriceCommand` (upsert by variant id).
- Inventory line routing is explicit via `op` (`CREATE`, `ADJUST`, `DAMAGE`, `WRITE_OFF`, `REORDER`). Omit lines with no inventory intent. At most one stock operation per `inventoryItemId` per request. Optional `reorder` may ride on `ADJUST` / `DAMAGE` / `WRITE_OFF`.
- Skip `ensure-product-view` when no SKUs were added (`ProductVariantViewProjectedEvent` fires only on variant add).
- Skip price or inventory steps when those lists are empty.
- Compensation deletes **only price sets created in this run**. In-place catalog/price/stock updates are left as-is. New inventory items are not rolled back (same as create). The product is never deleted.
- Removed variants are not a saga step. Catalog `FULL_SYNC` removes them; inventory already cascades on `ProductVariantDeletedIntegrationEvent`; pricing adds the same choreography.
- Terminal UI: reuse `WorkflowTerminalUiEvent` after `COMPLETED` / `FAILED` / `COMPENSATED` (ADR-009).

## Context shape (input vs progress)

**Input (start):** `merchantId`, `createdBy`, `scopeKey`, `scopeId`, `productId`, product metadata + `variantSync` (includes `matrixKey`), `pricingLines` (`sku`, optional `variantId`, money fields), `inventoryLines` (`op` + nested `create` / `adjust` / `damage` / `writeOff` / `reorder`; `inventoryItemId` required except `CREATE`).

**Progress:** `variantRefs`, `addedSkus`, `projectedSkus`, `pricePairs`, `createdPriceSetIds`, `inventoryItemIds`, `createdInventoryItemIds`.

## Consequences

- Edit save is one tracked run with HATEOAS rel `update-sellable-product` (never `edit-*`).
- Failure after in-place catalog/price/stock updates does not restore the previous snapshot. Damage, write-off, and adjust are not compensated; `idempotencyKey` is the duplicate-POST guard (those movements are not naturally idempotent if the listener re-ran the command).
- Location moves on existing inventory items are out of scope; existing items ignore `locationId`.
- Descriptions, media, and status stay on existing catalog endpoints.
