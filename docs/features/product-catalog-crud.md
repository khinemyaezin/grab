# Feature: Product Catalog CRUD

## 1. Problem Statement

The system needs to create, retrieve, search, and delete products with
multi-variant support. Each product can have multiple variants defined by
combinations of variation types (e.g., Color, Size), and the system must
generate and validate these combinations.

---

## 2. Business Scope

### In scope
- Create products with variants and variations
- Retrieve single product with full variant details
- Search products with filtering and pagination
- Generate variant combinations from variation types
- Delete products (with domain event publishing)
- Soft-delete variants (batch, via product save)
- SKU uniqueness enforcement within a product
- Variant uniqueness enforcement via Specification pattern

### Out of scope
- Product update (separate feature)
- Product lifecycle status (separate feature)
- Pricing
- Inventory integration
- Media / image management
- Product descriptions

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Product
- Type: Aggregate Root

### Related concepts

| Name                  | Type           | Notes                                        |
|-----------------------|----------------|----------------------------------------------|
| ProductVariant        | Entity         | Child of Product; holds SKU, status           |
| ProductVariation      | Value Object   | Single type–option pair (e.g., Color: Red)    |
| VariantType           | Entity         | Variation type (e.g., Color, Size)            |
| VariantOption         | Entity         | Option within a type (e.g., Red, Blue)        |
| VariantCombination    | Value Object   | Ordered list of ProductVariation              |
| ProductVariantStatus  | Enum           | ACTIVE, DELETED                               |
| Category              | Aggregate Root | Referenced by Category ID only                |

---

## 4. Aggregate Boundary

### Aggregate Root
- Product

### Inside boundary
- ProductVariant (Entity)
- ProductVariation (Value Object)

### Outside boundary (reference only)
- Category (by ID)
- VariantType / VariantOption (used during combination generation, not persisted inside Product)
- Inventory
- Media
- Description

---

## 5. Invariants

- Product name must not be null
- Category ID must not be null
- A variant's SKU must be unique within the product (enforced by UniqueSkuSpec)
- A variant's variation set must be unique within the product (enforced by UniqueProductVariantSpec)
- Variant uniqueness is validated via CompositeSpecification before adding
- Variant combination generation is capped at 100,000 combinations
- Combination generation must preserve input order of types and options

---

## 6. Domain Operations

### Product operations

| Operation            | Description                                  | Enforces Invariants              |
|----------------------|----------------------------------------------|----------------------------------|
| create               | Create product with name and category        | name not null, category not null |
| addVariant           | Add a variant with uniqueness check          | SKU + variation uniqueness       |
| updateVariant        | Replace variant at same index                | variant exists                   |
| removeVariant        | Hard-delete a variant by ID                  | publishes event                  |
| applySoftDeleteVariants | Batch soft-delete by variant IDs          | marks as DELETED                 |
| changeCategory       | Change product category                      | publishes CategoryChangedEvent   |
| delete               | Mark product for deletion                    | publishes ProductDeletedEvent    |

### Domain services

| Service                       | Description                                              |
|-------------------------------|----------------------------------------------------------|
| VariantCombinationService     | Generates all variant combinations from types and options |
| VariationCombinationManager   | Syncs new combinations with existing variants (NEW, EXTENDED, UNCHANGED) |
| VariantDeletionStrategy       | Filters types and removes obsolete variants              |
| VariationKeyGenerator         | Generates unique keys from variation sets for comparison  |
| SkuGenerator                  | Generates SKU from product name and variations            |

---

## 7. Domain Model

### Product (Aggregate Root)

```java
public class Product extends AggregateRoot<Id> {
    private String name;
    private Id categoryId;
    private List<ProductVariant> variants;
}
```

- Creation: `Product.create(Id, String name, Id categoryId)`
- Reconstitution: `new Product(Id, String, Id, List<ProductVariant>)`

### ProductVariant (Entity)

```java
public class ProductVariant extends Entity<Id> {
    private String sku;
    private Set<ProductVariation> variations;  // LinkedHashSet, order preserved
    private ProductVariantStatus status;       // ACTIVE or DELETED
}
```

- Creation: `ProductVariant.create(Id, String sku, List<ProductVariation>)` — defaults to ACTIVE

### ProductVariation (Value Object)

```java
public class ProductVariation extends ValueObject {
    private String optionName;   // e.g., "Red"
    private Id optionId;
    private Id typeId;
    private String typeName;     // e.g., "Color"
}
```

- Equality: based on `optionId` + `typeId`

### Specifications

| Specification                      | Validates                                        |
|------------------------------------|--------------------------------------------------|
| UniqueProductVariantCompositeSpec  | Composite entry point for variant uniqueness      |
| UniqueProductVariantSpec           | Variation set is unique across existing variants  |
| UniqueSkuSpec                      | SKU is unique across existing variants            |
| CombinationSpec                    | Combination validity                              |
| VariationSizeSpec                  | Variation size constraints                        |

---

## 8. Creation vs Reconstitution

### Creation
- Method: `Product.create(id, name, categoryId)`
- Validations:
    - name not null
    - categoryId not null
- Variants added individually via `addVariant()` with specification check

### Reconstitution
- Method: `new Product(id, name, categoryId, variants)`
- Assumptions:
    - Persisted data is already valid
    - Specifications are not re-evaluated

---

## 9. Persistence & Mapping Strategy

### Repository
- `ProductRepository.save(Product)` — persists aggregate with variants
- `ProductRepository.find(Id)` — retrieves full aggregate
- `ProductRepository.delete(Product)` — removes aggregate
- `ProductQueryRepository` — summary queries with JPQL (read side)

### Assembler
- `ProductJpaAssembler` — maps between domain aggregate and JPA entity graph
- Handles full entity graph: Product → ProductVariant → ProductVariation

### JPA Entities
- `ProductEntity` — product table
- `ProductVariantEntity` — product_variant table (ManyToOne to Product)
- `ProductVariationEntity` — product_variation table (ManyToOne to ProductVariant)
- `VariantTypeEntity` — variant_type table
- `VariantOptionEntity` — variant_option table

### Mapping rules
- Repositories do not perform mapping
- Assembler coordinates all mappers (MapStruct)
- Domain events published after `save()` via `DomainEventProducer` (Spring ApplicationEventPublisher)

---

## 10. Application Services

### CQRS Architecture

Commands and queries are dispatched via `CommandBus` and `QueryBus` (SimpleCommandBus / SimpleQueryBus).
Handlers are auto-registered via Spring constructor injection.

### Commands

| Command               | Handler                      | Description                    |
|-----------------------|------------------------------|--------------------------------|
| `SaveProductCommand`  | `SaveProductCommandHandler`  | Create product with variants   |
| `DeleteProductCommand`| `DeleteProductCommandHandler`| Delete product by ID           |

### Queries

| Query                     | Handler                          | Description                                   |
|---------------------------|----------------------------------|-----------------------------------------------|
| `GetProductQuery`         | `GetProductQueryHandler`         | Get single product with variants and types     |
| `ProductSummaryQuery`     | `ProductSummaryQueryHandler`     | Search products with filters and pagination    |
| `ProductCombinationQuery` | `ProductCombinationQueryHandler` | Generate variant combinations from types       |

### Facade

`ProductFacadeService` orchestrates:
1. DTO → Command/Query mapping
2. Bus dispatch
3. Result → Response mapping
4. HATEOAS assembly

---

## 11. API Endpoints

| Method   | Endpoint                         | Request Body                  | Response                           |
|----------|----------------------------------|-------------------------------|------------------------------------|
| `POST`   | `/api/v1/products`               | `SaveProductRequest`          | `201` with Location header         |
| `GET`    | `/api/v1/products/{productId}`   | —                             | `GetProductResponse` (HATEOAS)     |
| `POST`   | `/api/v1/products/search`        | `ProductSummaryRequest`       | `ProductSummaryResponse` (paginated, HATEOAS) |
| `POST`   | `/api/v1/products/combination`   | `ProductCombinationRequest`   | `ProductCombinationResponse` (HATEOAS) |
| `DELETE` | `/api/v1/products/{productId}`   | —                             | `DeleteProductResponse` (HATEOAS)  |

### SaveProductRequest
```json
{
  "product": {
    "id": "prod-001",
    "name": "Classic T-Shirt",
    "categoryId": "cat-001",
    "variants": [
      {
        "id": "var-001",
        "sku": "TSHIRT-RED-S",
        "status": "ACTIVE",
        "variations": [
          { "optionName": "Red", "optionId": "opt-1", "typeId": "type-1", "typeName": "Color" },
          { "optionName": "S", "optionId": "opt-3", "typeId": "type-2", "typeName": "Size" }
        ]
      }
    ]
  },
  "variantTypes": [
    {
      "typeId": "type-1",
      "typeName": "Color",
      "options": [
        { "optionId": "opt-1", "optionName": "Red" },
        { "optionId": "opt-2", "optionName": "Blue" }
      ]
    }
  ]
}
```

### ProductSummaryRequest
```json
{
  "productName": "T-Shirt",
  "sku": "",
  "variantStatus": "ACTIVE",
  "variations": [],
  "page": 0,
  "size": 20
}
```

### GetProductResponse
```json
{
  "id": "prod-001",
  "name": "Classic T-Shirt",
  "categoryId": "cat-001",
  "variants": [
    {
      "id": "var-001",
      "sku": "TSHIRT-RED-S",
      "status": "ACTIVE",
      "variations": [
        { "optionId": "opt-1", "optionName": "Red", "typeId": "type-1", "typeName": "Color" }
      ]
    }
  ],
  "variantTypes": [
    {
      "typeId": "type-1",
      "typeName": "Color",
      "options": [
        { "optionId": "opt-1", "optionName": "Red" }
      ]
    }
  ]
}
```

---

## 12. Integration & Side Effects

### Domain Events

| Event                       | Published When                    | Payload                               |
|-----------------------------|-----------------------------------|---------------------------------------|
| `ProductDeletedEvent`       | Product is deleted                | productId, categoryId, variantIds     |
| `ProductVariantChangeEvent` | Variant is updated                | sku                                   |
| `ProductVariantDeletedEvent`| Variant is removed                | productId, categoryId, variantId      |
| `CategoryChangedEvent`      | Product category changes          | oldCategoryId, newCategoryId, productId |

### Event publishing
- Events collected in `AggregateRoot.getEvents()` during domain operations
- Published via `ApplicationDomainEventProducer` (Spring ApplicationEventPublisher) after repository save
- Events are in-memory only (not persisted to event store)

---

## 13. Error Scenarios

| Scenario                        | Handling                         |
|---------------------------------|----------------------------------|
| Product not found               | `ResourceNotFoundException`      |
| Duplicate SKU within product    | Specification rejects; `addVariant()` returns false |
| Duplicate variation combination | Specification rejects; `addVariant()` returns false |
| Combination limit exceeded      | `IllegalArgumentException` (>100,000 combinations) |
| No handler for command/query    | `IllegalArgumentException` from bus |
| Duplicate command handler        | `IllegalStateException` on startup |

---

## 14. Implementation Order

Already implemented:

1. Framework module (AggregateRoot, Entity, ValueObject, Specification, Id)
2. Product aggregate with variant management
3. Specification pattern (UniqueProductVariantCompositeSpec, UniqueSkuSpec)
4. Domain services (VariantCombinationService, VariationCombinationManager, SkuGenerator)
5. Domain events (ProductDeletedEvent, ProductVariantChangeEvent, etc.)
6. JPA entities and mappers (ProductEntity, ProductVariantEntity, etc.)
7. ProductJpaRepository with assembler
8. CQRS infrastructure (CommandBus, QueryBus, handlers)
9. ProductController with 5 endpoints
10. ProductFacadeService with HATEOAS assemblers
11. Unit and integration tests

---

## 15. Definition of Done

- [x] Product aggregate with variant management
- [x] Specification pattern for uniqueness validation
- [x] Domain services for combination generation
- [x] Domain events published on state changes
- [x] JPA persistence with assembler mapping
- [x] CQRS command/query separation
- [x] REST endpoints with HATEOAS
- [x] Search with filtering and pagination
- [x] Unit tests for domain logic
- [x] Integration tests for persistence
