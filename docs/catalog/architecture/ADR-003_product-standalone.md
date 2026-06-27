# ADR-007: Standalone Product

## Status
Proposed

## Context

Create requests may omit `product.variants`. Persisting products without variants increases downstream complexity and creates inconsistent behavior for simple listings.

We need create-time behavior that:
- supports explicit multi-variant payloads
- supports simple products without forcing clients to send synthetic variants

## Decision

For `SaveProduct` (create):

1. Use explicit `product.variants` when provided.
2. If variants are empty or null, create one default variant.

Additional behavior:
- auto-materialized variants get generated IDs and SKU values
- auto-materialized variants are `ACTIVE`
- fallback default variant is represented with one synthetic variation:
  - `typeName = "Title"`
  - `optionName = "Default Title"`
  - `typeId = "system:type:title"`
  - `optionId = "system:option:default-title"`
- duplicate add attempts fail fast rather than being silently ignored
- `variantTypes` remains in request/command shape but is not used by save materialization.

This decision is scoped to create-time only and does not introduce a global non-empty-variants invariant for all product mutations.

## Consequences

### Positive
- predictable create behavior for both simple and variant-rich listings
- fewer client responsibilities for bootstrap payloads
- no schema migration required

### Negative
- save behavior becomes richer and less "pure pass-through"
- generated SKU quality still depends on existing SKU generation policy

## Alternatives Considered

1. Reject create without variants  
Rejected because it forces unnecessary client-side synthetic variant creation.

2. Enforce non-empty variants globally (`create/update/sync/delete`)  
Deferred because it has larger behavioral impact on existing mutation flows.