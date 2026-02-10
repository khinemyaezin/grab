# ADR-002: Category as Aggregate Root with Hierarchical Operations

## Status
Accepted

## Context

The system requires full lifecycle management of product categories,
including hierarchical organization, tree queries, and reordering.
Previously, category behavior was partially missing , 
making hierarchy changes risky and hard to reason about.

Category operations such as moving nodes and fetching trees require
strong consistency and explicit invariant enforcement.

---

## Decision

Implement Category as a dedicated Aggregate Root responsible for:

- Maintaining parent–child hierarchy
- Enforcing tree-related invariants
- Handling move operations within the aggregate

Expose category behavior through application services and REST endpoints.
Use an Aggregate Assembler to reconstitute the category from persistence
models.

---

## Consequences

### Positive
- Clear ownership of hierarchy rules
- Safer category move operations
- Consistent category tree behavior

### Negative
- Additional complexity in aggregate design
- More upfront modeling effort

## Notes

- Products reference categories by ID only
- Category queries may use optimized read models
- Tree structure may evolve without breaking API contracts
