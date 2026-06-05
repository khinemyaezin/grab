# ADR-008: Product Lifecycle Status, Slug, and Storefront Query Support

## Status
Proposed

## Context

The Product aggregate currently supports create, read, search, and delete.
It has no lifecycle status — products are either present or deleted.
There is no way to:

- Keep a product in draft while configuring variants
- Archive a product without deleting it (preserving order history references)
- Look up a product by a SEO-friendly slug
- Query featured products or browse by category from a storefront

Additionally, individual variant updates (SKU changes) and soft-delete /
restore operations are not exposed through the API, even though the domain
model already supports `markAsDeleted()` and `activate()` on `ProductVariant`.

---

## Decision

### 1. Add `ProductStatus` enum to the Product aggregate

Introduce a status field with controlled transitions:

```
DRAFT → ACTIVE → ARCHIVED
```

- New products default to `DRAFT`
- Only `ACTIVE` products are visible on storefront queries
- `ARCHIVED` products are hidden but retained for historical references
- Transitions are enforced in the aggregate; invalid transitions are rejected

### 2. Add `slug` and `featured` fields to the Product aggregate

- `slug`: a unique, SEO-friendly identifier auto-generated from the product name
- `featured`: a boolean flag to mark products for storefront promotion

### 3. Expose variant-level operations through dedicated endpoints

- `PUT /products/{id}/variants/{variantId}` — update SKU
- `DELETE /products/{id}/variants/{variantId}` — soft-delete a single variant
- `POST /products/{id}/variants/{variantId}/restore` — restore a soft-deleted variant

These operations use existing domain behavior (`ProductVariant.markAsDeleted()`,
`ProductVariant.activate()`) that was previously not reachable via the API.

### 4. Add storefront query endpoints

- `GET /products/{id}/full` — full product detail (variants + types) for product pages
- `GET /products/category/{categoryId}` — browse products by category (paginated)
- `GET /products/slug/{slug}` — lookup by SEO-friendly URL
- `GET /products/featured` — featured products listing (paginated)

These queries only return `ACTIVE` products on storefront-facing endpoints.

---

## Consequences

### Positive
- Products have a clear lifecycle visible to both admin and storefront
- Archiving preserves data integrity for order history
- Slug enables SEO-friendly URLs without exposing internal IDs
- Featured flag enables curated storefront experiences
- Variant-level operations reduce the need to re-submit the entire product

### Negative
- Additional fields on the Product aggregate and persistence layer
- Slug uniqueness requires enforcement at both domain and database level
- Status transitions add validation complexity to the aggregate
- Storefront queries may need optimized read models as data grows

---

## Notes

- Pricing is intentionally excluded from this ADR and will be addressed separately
- Media and description management are separate features
- Status transition validation lives in the `ProductStatus` enum via `canTransitionTo()`
- Slug generation strategy (e.g., name-based with collision handling) is an implementation detail
- Storefront queries should filter by `status = ACTIVE` at the query/repository level
