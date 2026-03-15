# Feature: Catalog Lifecycle, Integrity, and SKU Policy

## 1. Problem Statement

The catalog must enforce commerce-safe lifecycle and identity rules.
Before this feature, an active product could become unsellable after variant
deletion or sync, product deletion could bypass aggregate lifecycle handling,
category deletion could orphan products, and SKU uniqueness was enforced only
inside a single product instead of across the catalog.

---

## 2. Business Scope

### In scope
- Prevent deletion of the last active variant from an active product
- Archive active products when they become unsellable
- Make product delete archive-first instead of hard-delete
- Validate category existence on product create and update
- Block category subtree deletion when products are assigned
- Emit consistent domain events for variant soft-delete, restore, status change, and product delete
- Enforce SKU uniqueness globally across all product variants

### Out of scope
- Automatic category reassignment during delete
- Inventory reservation or stock validation
- Pricing or channel-specific publication
- Physical database migration tooling beyond entity/repository support

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Product
- Type: Aggregate Root

### Related concepts

| Name | Type | Notes |
|------|------|------|
| ProductVariant | Entity | Child of Product; carries SKU and lifecycle state |
| ProductStatus | Enum | `DRAFT`, `ACTIVE`, `ARCHIVED` |
| Category | Aggregate Root | Referenced by `categoryId` |
| SKU | Cross-context identity | Must be globally unique |

---

## 4. Aggregate Boundary

### Aggregate Root
- Product

### Inside boundary
- ProductVariant
- ProductStatus

### Outside boundary (reference only)
- Category
- Search
- Inventory
- Order

---

## 5. Invariants

### Product lifecycle
- An `ACTIVE` product must always have at least one active variant
- A product cannot be activated without active variants
- A product delete operation archives the product first
- Storefront visibility depends on `ACTIVE` status and at least one active variant

### Category integrity
- Product create must reference an existing category
- Product update must reference an existing category
- Category subtree deletion is rejected if any product is assigned to any category in that subtree

### SKU identity
- SKU is treated as a global identity across product variants
- No two variants in the catalog may share the same SKU
- Request-level duplicate SKUs are rejected before persistence

---

## 6. Domain Operations

| Operation | Description | Enforces Invariants |
|-----------|-------------|---------------------|
| `changeStatus` | Publish, unpublish, or archive a product | valid status transitions, active variants required for `ACTIVE` |
| `applySoftDeleteVariants` | Soft-delete selected variants | prevents last active variant deletion, emits delete events |
| `removeVariant` | Remove variants during sync | prevents last active variant deletion, archives unsellable products |
| `archiveIfUnsellable` | Downgrade active products with no active variants | active product must remain sellable |
| `delete` | Archive-first product deletion | product must not bypass lifecycle handling |

---

## 7. Application Rules

### Product create and update
- `SaveProductCommandHandler` validates the category exists
- `UpdateProductCommandHandler` validates the category exists
- Both flows resolve slug normally, but now also enforce catalog-wide SKU uniqueness for supplied variants

### Variant management
- `DeleteVariantCommandHandler` soft-deletes variants through the aggregate
- `RestoreVariantCommandHandler` restores deleted variants through the aggregate
- `UpdateVariantCommandHandler` rejects duplicate SKUs already used by another variant
- `SyncVariantsCommandHandler` validates target SKUs globally and archives the product first when the target set would leave it unsellable

### Category delete
- `DeleteCategoryCommandHandler` loads the full subtree
- Delete is blocked if any product references any category inside that subtree
- Reassignment is not implemented in this feature; blocking is the current behavior

### Product delete
- `DeleteProductCommandHandler` no longer hard-deletes the row directly
- It calls the aggregate delete flow, which archives first and then emits delete events

---

## 8. Status Transition Policy

```java
public enum ProductStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED
}
```

### Allowed transitions
- `DRAFT -> ACTIVE`
- `DRAFT -> ARCHIVED`
- `ACTIVE -> ARCHIVED`
- `ARCHIVED -> ACTIVE`

### Notes
- `ARCHIVED -> ACTIVE` is allowed only when the product has at least one active variant
- This supports explicit republish after archive

---

## 9. Event Contract

### Domain events emitted

| Event | Trigger |
|-------|---------|
| `ProductStatusChangedEvent` | Product publish, unpublish, archive, or republish |
| `ProductVariantDeletedEvent` | Variant soft-delete or variant removal during sync |
| `ProductVariantRestoredEvent` | Variant restore |
| `ProductDeletedEvent` | Product delete flow after archive-first lifecycle handling |
| `ProductUpdatedEvent` | Product metadata update |
| `CategoryChangedEvent` | Product category change |

### Event intent
- downstream modules can react to lifecycle changes without inspecting catalog tables directly
- delete and restore operations are visible as explicit domain events
- publish/archive behavior is expressed through status change events instead of ad hoc flags

---

## 10. Persistence and Repository Strategy

### Repository additions
- `ProductRepository.isSkuTaken(String sku, String excludeVariantUuid)`
- `ProductRepository.existsByCategoryIds(Collection<Id> categoryIds)`
- `CategoryRepository.findSubtreeIds(Id id)`

### Persistence behavior
- `ProductVariantEntity.sku` is marked unique
- product/category integrity checks are enforced at application level before destructive operations
- subtree category checks are resolved from the nested-set category structure

---

## 11. Error Policy

### Domain errors
- `cat.domain.product_activation_requires_active_variants`
- `cat.domain.cannot_delete_last_active_variant_from_active_product`
- `cat.domain.invalid_product_status_transition`

### Service errors
- `cat.service.category.not_found`
- `cat.service.category.has_assigned_products`
- `cat.service.variant.sku_already_exists`

---

## 12. Acceptance Scenarios

### Lifecycle
- deleting the only active variant of an active product is rejected
- syncing an active product to zero active variants archives the product
- deleting a product archives it first and does not hard-delete it directly

### Integrity
- creating a product with a missing category fails
- updating a product to a missing category fails
- deleting a category subtree with assigned products fails

### Events
- soft-delete emits `ProductVariantDeletedEvent`
- restore emits `ProductVariantRestoredEvent`
- publish/archive emits `ProductStatusChangedEvent`
- delete emits `ProductDeletedEvent`

### SKU policy
- duplicate SKU in product create fails
- duplicate SKU in variant update fails
- duplicate SKU in variant sync fails

---

## 13. Notes

- This feature hardens the existing catalog bounded context without moving pricing or inventory concerns into the Product aggregate.
- Category delete is intentionally conservative for now. Reassignment can be added later as a separate feature with explicit admin workflow and audit rules.
- Global SKU uniqueness is chosen because SKU is being treated as a cross-context identity for inventory and downstream commerce flows.
