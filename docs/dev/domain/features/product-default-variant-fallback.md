# Feature: Product Default Variant Fallback on Create

## 1. Problem Statement

Create requests can arrive without explicit variants. Today that may produce products with no variants, which is awkward for sellability and inconsistent with a simple-listing model.

We need deterministic create behavior that always materializes variants, while still supporting full variant matrices.

---

## 2. Decision Summary

On `POST /api/v1/products`, apply hybrid materialization:

1. If `product.variants` is provided and non-empty, use it.
2. If `product.variants` is empty or null, create one standalone default variant (active, generated id/sku) using synthetic Shopify-style variation values:
   - `typeName = "Title"`
   - `optionName = "Default Title"`
   - `typeId = "system:type:title"`
   - `optionId = "system:option:default-title"`

Scope is create-time only. This feature does not enforce a global "product must always keep at least one variant" invariant for update/sync/delete flows.

---

## 3. API Semantics

### Endpoint
- `POST /api/v1/products`

### Request shape
- Existing request shape is unchanged.
- `product.variants` may be empty/null.

### Semantics
- Explicit variants take precedence over default fallback.
- Generated/default variants use generated IDs and generated SKU values.
- Generated/default variants are persisted as `ACTIVE`.
- Default fallback uses one synthetic variation (`Title` / `Default Title`) instead of an empty variation set.

---

## 4. Domain Rules and Validation

- SKU uniqueness is still enforced (request + repository check).
- Duplicate combination add attempts fail fast (conflict), instead of silently dropping variants.

---

## 5. Examples

### A. Explicit variants provided
- Input: `product.variants` has items
- Outcome: save those variants (after validation)

### B. Variants empty
- Input: `product.variants=[]`
- Outcome: create one default variant with synthetic `Title / Default Title` variation
---

## 6. Test Scenarios

- Create with explicit variants keeps explicit variants.
- Create with empty/null variants creates one standalone default variant with synthetic IDs/names.
- Duplicate explicit variant combination returns conflict (`variant.add_failed`).
