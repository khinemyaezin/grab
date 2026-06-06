# ADR-006: Product as Aggregate Root with Variant Model and CQRS

## Status
Accepted

## Context

The system needs to manage products where each product can have multiple
variants defined by combinations of variation types (e.g., Color: Red,
Size: S). Variant combinations can grow exponentially, and uniqueness must
be enforced at the domain level.

Key challenges:
- A product's variants must be managed as a consistent unit
- SKU and variation combination uniqueness must be enforced before persistence
- Variant combination generation must be order-preserving and bounded
- Read operations (search, summaries) have different needs than write operations (create, delete)
- The catalog module must remain decoupled from other bounded contexts

---

## Decision

### 1. Product as Aggregate Root with ProductVariant as child Entity

Product owns its variants. All variant mutations (add, update, remove,
soft-delete) go through the Product aggregate, ensuring invariants are
checked before any state change.

- `ProductVariant` is an Entity (has identity via ID and SKU)
- `ProductVariation` is a Value Object (identity-less, equality by optionId + typeId)
- `VariantType` and `VariantOption` are reference entities used during
  combination generation but not persisted inside the Product aggregate

### 2. Specification pattern for domain validation

Variant uniqueness is enforced using composable specifications rather than
repository queries or database constraints alone:

- `UniqueProductVariantCompositeSpec` — entry point
- `UniqueProductVariantSpec` — variation set uniqueness
- `UniqueSkuSpec` — SKU uniqueness

This keeps validation logic in the domain layer and makes it testable
without infrastructure.

### 3. Domain services for combination logic

Variant combination generation is handled by domain services, not the
aggregate itself:

- `VariantCombinationService` — generates all combinations from types/options
  with order preservation and a 100,000 combination limit
- `VariationCombinationManager` — syncs new combinations against existing
  variants, classifying each as NEW, EXTENDED, or UNCHANGED
- `VariantDeletionStrategy` — determines which variants to remove when
  types change

This separates complex algorithmic logic from aggregate state management.

### 4. CQRS with in-process command and query buses

Commands (writes) and queries (reads) are separated via:

- `CommandBus` / `SimpleCommandBus` — dispatches to `CommandHandler` implementations
- `QueryBus` / `SimpleQueryBus` — dispatches to `QueryHandler` implementations
- Handlers are auto-registered via Spring constructor injection
- Commands are `@Transactional`; queries are read-only

This allows write-side and read-side to evolve independently. The read side
uses dedicated JPQL queries (`ProductQueryJpqlRepository`) for summary views,
while the write side operates on the full aggregate graph.

### 5. Domain events via Spring ApplicationEventPublisher

State changes publish domain events:

- Events are collected in `AggregateRoot.getEvents()` during operations
- Published after `save()` via `ApplicationDomainEventProducer`
- Currently in-memory only (not persisted to an event store)

This enables loose coupling between modules without introducing a message
broker dependency upfront.

---

## Consequences

### Positive
- Strong consistency within the Product aggregate boundary
- Domain validation is testable without database
- Combination logic is reusable and isolated in domain services
- CQRS enables independent optimization of reads vs writes
- Domain events allow future module integration without coupling

### Negative
- Aggregate loads all variants on every write (may need optimization for products with many variants)
- In-memory events are lost on application failure
- CQRS adds indirection (command/query → bus → handler → repository)
- Specification pattern requires new classes for each validation rule

---

## Notes

- Products reference categories by ID only (no cross-aggregate navigation)
- The Catalog module is declared via Spring Modulith (`@ApplicationModule`)
- MapStruct is used for all mapping layers (JPA ↔ Domain ↔ DTO)
- HATEOAS links are added at the controller layer via model assemblers
- The `ProductFacadeService` acts as the orchestration layer between REST and CQRS
