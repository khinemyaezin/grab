# ADR-009: Sync Product Variants by Desired Combinations

## Status
Proposed

## Context

Product variants are currently created at product creation time and can be
updated or deleted one-at-a-time. When a merchant changes variant types or
options (e.g., adds a new color or removes a size), the system needs a
consistent way to regenerate variant combinations and reconcile them with
existing variants.

Without a bulk sync operation, clients must either:

- Manually compute all combinations and call multiple endpoints, or
- Recreate the product to reflect new variant options

This creates drift between the variant option set and the actual variants
stored for a product.

---

## Decision

Introduce a bulk sync endpoint to align a product's variants with the
currently desired set of variant types/options and their combinations.

### API

- `PUT /api/v1/products/{productId}/variants`

### Request

- `variantTypes`: list of variant types and their options
- `variants`: list of variants, each with an id, sku, and the full set of
  variations that define its combination

### Behavior

1. Generate all desired combinations from `variantTypes`.
2. Filter out options that are fully deleted (via `VariantDeletionStrategy`).
3. Diff desired combinations against existing variants using the
   `VariationCombinationManager` and `VariationKeyGenerator`.
4. For each desired combination, require a matching request variant; if
   missing, the sync fails.
5. Update existing variants or add new ones; remove any variants that are
   not part of the desired combinations.
6. All synced variants are set to `ACTIVE`.

This operation becomes the canonical way to reconcile the variant set after
variant-type/option changes.

---

## Consequences

### Positive
- Single endpoint to keep variants aligned with desired combinations
- Simplifies UI workflows for editing options and regenerating variants
- Prevents orphaned variants when options are removed
- Ensures SKU and combination uniqueness via existing domain specs

### Negative
- Clients must send the full desired variant list every time
- Large option sets can produce many combinations (bounded by existing
  combination generation limits)
- Sync failures are all-or-nothing when any combination is missing

---

## Notes

- The sync endpoint trusts request variant IDs for a given combination
  (IDs can change even when the combination is the same).
- Errors are raised for duplicate combination keys or missing combinations
  in the request payload.
