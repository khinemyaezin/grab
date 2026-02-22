# Feature: Product Variant Sync

## 1. Problem Statement

When variant types or options change, the system must regenerate variant
combinations and reconcile them with existing variants. The current API only
supports per-variant updates, which makes bulk changes error-prone and
inconsistent for admins and storefront tooling.

---

## 2. Business Scope

### In scope
- Sync variants to match the current set of variant types and options
- Add new variants for newly generated combinations
- Update existing variants (SKU, ID) for existing combinations
- Remove variants that are no longer part of the desired combinations
- Return a normalized response of variants and variant types

### Out of scope
- Partial variant updates without providing the full desired set
- Pricing, inventory, or media changes
- Variant status management beyond setting synced variants to ACTIVE

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Product
- Type: Aggregate Root

### Related concepts

| Name              | Type         | Notes                                         |
|-------------------|--------------|-----------------------------------------------|
| ProductVariant    | Entity       | SKU and variation set                          |
| VariantType       | Entity       | e.g., Color, Size                              |
| VariantOption     | Entity       | Option within a type                           |
| ProductVariation  | Value Object | Type-option pair used to build combinations     |
| VariantCombination| Value Object | Ordered list of ProductVariation               |

---

## 4. Aggregate Boundary

### Aggregate Root
- Product

### Inside boundary
- ProductVariant
- ProductVariation

### Outside boundary (reference only)
- VariantType / VariantOption definitions (used for combination generation)

---

## 5. Invariants

- Each variant combination is unique within a product
- SKU must be unique within a product
- Request must include a variant for every desired combination
- Duplicate combination keys in the request are rejected
- Synced variants are set to `ACTIVE`

---

## 6. Domain Operations

| Operation          | Description                                            | Enforces Invariants           |
|-------------------|--------------------------------------------------------|-------------------------------|
| buildCombinations | Generate combinations from variant types/options       | combination size constraints  |
| syncVariants      | Add, update, and remove variants to match combinations | uniqueness specs              |
| filterOptions     | Remove fully deleted options before combination build  | deletion strategy             |

---

## 7. API Contract

### Endpoint
- `PUT /api/v1/products/{productId}/variants`

### Request

```json
{
  "variantTypes": [
    {
      "typeId": "type-color",
      "typeName": "Color",
      "options": [
        { "optionId": "opt-red", "optionName": "Red" },
        { "optionId": "opt-blue", "optionName": "Blue" }
      ]
    }
  ],
  "variants": [
    {
      "id": "var-red",
      "sku": "SKU-RED",
      "variations": [
        {
          "optionName": "Red",
          "optionId": "opt-red",
          "typeId": "type-color",
          "typeName": "Color"
        }
      ]
    }
  ]
}
```

### Response

```json
{
  "productId": "product-1",
  "productName": "T-Shirt",
  "variants": [
    {
      "id": "var-red",
      "sku": "SKU-RED",
      "status": "ACTIVE",
      "variations": [
        {
          "optionName": "Red",
          "optionId": "opt-red",
          "typeId": "type-color",
          "typeName": "Color"
        }
      ]
    }
  ],
  "variantTypes": [
    {
      "typeId": "type-color",
      "typeName": "Color",
      "options": [
        { "optionId": "opt-red", "optionName": "Red" },
        { "optionId": "opt-blue", "optionName": "Blue" }
      ]
    }
  ]
}
```

---

## 8. Application Flow

1. Map request to `SyncVariantsCommand`.
2. Filter variant types with `VariantDeletionStrategy`.
3. Generate desired combinations from filtered types.
4. Build a request lookup by variation key.
5. Materialize target variants from the desired combinations.
6. Update product variants by adding, updating, and removing as needed.
7. Persist product and return `SyncVariantsResponse`.

---

## 9. Validation and Errors

- Missing product results in a not-found error.
- Duplicate combination keys in the request throw an error.
- If a desired combination does not exist in the request, the sync fails.
- SKU and combination uniqueness are validated by domain specifications.

---

## 10. Notes

- The request is treated as the source of truth for variant IDs and SKUs.
- If a combination exists but the request provides a different variant ID,
  the existing variant is replaced with the requested ID.
- Combination generation is bounded by the existing limit in the
  combination service.
