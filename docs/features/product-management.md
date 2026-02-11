# Feature: Product Management

## 1. Problem Statement

The current Product API only supports create, read, search, and delete.
To operate as an e-commerce platform, the system must support full product
lifecycle management — updating products, controlling product visibility
through status transitions, and serving rich product detail pages to
storefronts.

---

## 2. Business Scope

### In scope
- Update product name and category
- Product lifecycle status (DRAFT, ACTIVE, ARCHIVED)
- Update individual variant (SKU, status)
- Soft-delete and restore variants
- Full product detail query for storefront
- Browse products by category
- SEO-friendly slug-based product lookup
- Featured product listing

### Out of scope
- Media / image upload (separate feature)
- Product description management (separate feature)
- Product features / attributes (separate feature)
- Inventory integration
- Pricing (separate feature)

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Product
- Type: Aggregate Root

### Related concepts

| Name               | Type           | Notes                                       |
|--------------------|----------------|---------------------------------------------|
| ProductVariant     | Entity         | Child of Product; holds SKU, status          |
| ProductVariation   | Value Object   | Color/Size combination on a variant          |
| ProductStatus      | Enum           | DRAFT, ACTIVE, ARCHIVED                      |
| Slug               | Value Object   | SEO-friendly URL identifier                  |
| Category           | Aggregate Root | Referenced by Category ID                    |

---

## 4. Aggregate Boundary

### Aggregate Root
- Product

### Inside boundary
- ProductVariant (Entity)
- ProductVariation (Value Object)
- ProductStatus (Enum — new)
- Slug (Value Object — new)

### Outside boundary (reference only)
- Category
- Inventory
- Media
- Description

---

## 5. Invariants

### Product-level
- Product name must not be blank
- Category ID must reference a valid category
- Slug must be unique across all products
- Status transitions must follow: DRAFT → ACTIVE → ARCHIVED
- ARCHIVED products cannot transition back to ACTIVE
- Only ACTIVE products are visible on storefront

### Variant-level
- SKU must be unique within the product
- A soft-deleted variant cannot be updated (must be restored first)
- Restoring a variant resets its status to ACTIVE

---

## 6. Domain Operations

### Product operations

| Operation       | Description                          | Enforces Invariants          |
|-----------------|--------------------------------------|------------------------------|
| update          | Update product name, category        | name not blank, category ref |
| changeStatus    | Transition product status            | valid state transition       |
| generateSlug    | Generate slug from product name      | uniqueness                   |

### Variant operations

| Operation       | Description                          | Enforces Invariants          |
|-----------------|--------------------------------------|------------------------------|
| updateVariant   | Update SKU on a variant              | SKU uniqueness               |
| softDelete      | Mark variant as DELETED              | variant exists, is active    |
| restore         | Restore soft-deleted variant         | variant exists, is deleted   |

### Query operations

| Operation              | Description                                  |
|------------------------|----------------------------------------------|
| getFullProduct         | Product + variants + types                    |
| getBySlug              | Lookup product by slug                        |
| getFeatured            | List featured/promoted products               |
| getByCategory          | List products in a category                   |

---

## 7. Domain Model Changes

### Product aggregate — new fields

```java
public class Product extends AggregateRoot<Id> {
    // existing
    private String name;
    private Id categoryId;
    private List<ProductVariant> variants;

    // new
    private ProductStatus status;      // DRAFT, ACTIVE, ARCHIVED
    private String slug;               // SEO-friendly URL identifier
    private boolean featured;          // Featured on storefront
}
```

### ProductStatus — new enum

```java
public enum ProductStatus {
    DRAFT,      // Not visible, still being configured
    ACTIVE,     // Visible on storefront
    ARCHIVED;   // Hidden, retained for order history

    public boolean canTransitionTo(ProductStatus target) {
        return switch (this) {
            case DRAFT    -> target == ACTIVE;
            case ACTIVE   -> target == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}
```

---

## 8. Creation vs Reconstitution

### Creation
- Method: `Product.create(id, name, categoryId)` — existing
- New: status defaults to `DRAFT`
- New: slug auto-generated from name

### Reconstitution
- Method: `new Product(id, name, categoryId, variants, status, slug, featured)`
- Assumptions:
    - Persisted data is already valid
    - Status transitions are not re-validated

---

## 9. Persistence & Mapping Strategy

### Schema changes

#### `product` table
```sql
ALTER TABLE product ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE product ADD COLUMN slug VARCHAR(255) UNIQUE;
ALTER TABLE product ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE;
```

### Repository
- `ProductRepository.find(Id)` — existing
- `ProductRepository.save(Product)` — existing (handles update)
- `ProductRepository.findBySlug(String slug)` — new
- `ProductQueryRepository.findByCategory(Id categoryId, PageInfo)` — new
- `ProductQueryRepository.findFeatured(PageInfo)` — new

### Assembler
- `ProductJpaAssembler` — update to map new fields (status, slug, featured)

### Mapping rules
- `ProductStatus` → stored as `VARCHAR`
- `Slug` → stored as `VARCHAR` with unique index

---

## 10. Application Services

### Commands

| Command                     | Handler                          | Description                                |
|-----------------------------|----------------------------------|--------------------------------------------|
| `UpdateProductCommand`      | `UpdateProductCommandHandler`    | Update product name, category              |
| `UpdateProductStatusCommand`| `UpdateProductStatusCommandHandler` | Change product lifecycle status         |
| `UpdateVariantCommand`      | `UpdateVariantCommandHandler`    | Update variant SKU                         |
| `DeleteVariantCommand`      | `DeleteVariantCommandHandler`    | Soft-delete a single variant               |
| `RestoreVariantCommand`     | `RestoreVariantCommandHandler`   | Restore a soft-deleted variant             |

### Queries

| Query                       | Handler                          | Description                                |
|-----------------------------|----------------------------------|--------------------------------------------|
| `GetFullProductQuery`       | `GetFullProductQueryHandler`     | Full product detail for storefront         |
| `GetProductBySlugQuery`     | `GetProductBySlugQueryHandler`   | Lookup product by slug                     |
| `GetFeaturedProductsQuery`  | `GetFeaturedProductsQueryHandler`| List featured products                     |
| `GetProductsByCategoryQuery`| `GetProductsByCategoryQueryHandler` | List products in a category             |

---

## 11. API Endpoints

### Product CRUD + Lifecycle

| Priority | Method  | Endpoint                           | Request Body                      | Response                    |
|----------|---------|------------------------------------|-----------------------------------|-----------------------------|
| P0       | `PUT`   | `/api/v1/products/{id}`            | `UpdateProductRequest`            | `200` with updated product  |
| P1       | `PATCH` | `/api/v1/products/{id}/status`     | `{ "status": "ACTIVE" }`         | `200` with new status       |

#### UpdateProductRequest
```json
{
  "name": "Updated Product Name",
  "categoryId": "cat-001"
}
```

#### UpdateProductStatusRequest
```json
{
  "status": "ACTIVE"
}
```

### Variant Management

| Priority | Method   | Endpoint                                              | Request Body              | Response                   |
|----------|----------|-------------------------------------------------------|---------------------------|----------------------------|
| P0       | `PUT`    | `/api/v1/products/{id}/variants/{variantId}`          | `UpdateVariantRequest`    | `200` with updated variant  |
| P1       | `DELETE` | `/api/v1/products/{id}/variants/{variantId}`          | —                         | `200` with confirmation    |
| P2       | `POST`   | `/api/v1/products/{id}/variants/{variantId}/restore`  | —                         | `200` with restored variant|

#### UpdateVariantRequest
```json
{
  "sku": "TSHIRT-RED-L"
}
```

### Storefront / Public APIs

| Priority | Method | Endpoint                                  | Query Params          | Response                            |
|----------|--------|-------------------------------------------|-----------------------|-------------------------------------|
| P0       | `GET`  | `/api/v1/products/{id}/full`              | —                     | `FullProductResponse` (everything)  |
| P1       | `GET`  | `/api/v1/products/category/{categoryId}`  | `page`, `size`        | `ProductSummaryResponse` (paginated)|
| P2       | `GET`  | `/api/v1/products/slug/{slug}`            | —                     | `FullProductResponse`               |
| P2       | `GET`  | `/api/v1/products/featured`               | `page`, `size`        | `ProductSummaryResponse` (paginated)|

#### FullProductResponse
```json
{
  "id": "prod-001",
  "name": "Classic T-Shirt",
  "slug": "classic-t-shirt",
  "status": "ACTIVE",
  "featured": true,
  "categoryId": "cat-001",
  "variants": [
    {
      "id": "var-001",
      "sku": "TSHIRT-RED-L",
      "status": "ACTIVE",
      "variations": [
        { "typeId": "type-1", "typeName": "Color", "optionId": "opt-1", "optionName": "Red" },
        { "typeId": "type-2", "typeName": "Size", "optionId": "opt-4", "optionName": "L" }
      ]
    }
  ],
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

---

## 12. Integration & Side Effects

### Domain Events

| Event                          | Published When                     | Consumers                     |
|--------------------------------|------------------------------------|-------------------------------|
| `ProductStatusChangedEvent`    | Product status transitions         | Search index, Cache           |
| `ProductUpdatedEvent`          | Product name or category changes   | Search index, Cache           |
| `ProductVariantDeletedEvent`   | Variant is soft-deleted            | Inventory (existing)          |
| `ProductVariantRestoredEvent`  | Variant is restored                | Inventory                     |

### Cross-module effects
- Archiving a product should not delete inventory records
- Status change to ACTIVE requires at least one active variant

---

## 13. Error Scenarios

| Scenario                              | Handling                                          |
|---------------------------------------|---------------------------------------------------|
| Product not found                     | `404 Not Found`                                   |
| Variant not found                     | `404 Not Found`                                   |
| Invalid status transition             | `422 Unprocessable Entity` with transition details |
| Duplicate slug                        | `409 Conflict`                                     |
| Update on soft-deleted variant        | `409 Conflict` — must restore first               |
| Restore on already active variant     | `409 Conflict` — variant is not deleted            |
| Activate product with no active variant | `422 Unprocessable Entity`                       |

---

## 14. Implementation Order

### Phase 1 — P0 (Core)
1. `ProductStatus` enum with transition rules
2. Add status, slug, featured to `Product` domain
3. Schema migration (`product` table)
4. Update `ProductJpaAssembler` for new fields
5. `UpdateProductCommand` + handler
6. `UpdateVariantCommand` + handler
7. `GetFullProductQuery` + handler
8. REST endpoints: `PUT /products/{id}`, `PUT /products/{id}/variants/{variantId}`, `GET /products/{id}/full`
9. Tests

### Phase 2 — P1
1. `UpdateProductStatusCommand` + handler (with transition validation)
2. `DeleteVariantCommand` + handler (single variant soft-delete)
3. `GetProductsByCategoryQuery` + handler
4. REST endpoints: `PATCH /products/{id}/status`, `DELETE /products/{id}/variants/{variantId}`, `GET /products/category/{categoryId}`
5. Tests

### Phase 3 — P2
1. `RestoreVariantCommand` + handler
2. `GetProductBySlugQuery` + handler
3. `GetFeaturedProductsQuery` + handler
4. REST endpoints: `POST /products/{id}/variants/{variantId}/restore`, `GET /products/slug/{slug}`, `GET /products/featured`
5. Tests

---

## 15. Definition of Done

- [ ] All invariants enforced in domain
- [ ] Status transitions validated in aggregate
- [ ] Slug uniqueness enforced
- [ ] Repositories handle new fields
- [ ] CQRS commands and queries implemented
- [ ] REST endpoints with validation
- [ ] Domain events published for state changes
- [ ] Unit tests for domain logic
- [ ] Integration tests for persistence
- [ ] API integration tests