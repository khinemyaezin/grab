# Feature: Category Management

## 1. Problem Statement

We are implementing Category management so that the system can create,
organize, and query product categories in a hierarchical structure.

---

## 2. Business Scope

### In scope
- Create, update, delete categories
- Support parent–child hierarchy
- Retrieve category trees
- Move categories within the hierarchy

### Out of scope
- Inventory rules
- Pricing logic

---

## 3. Domain Concept Identification

### Primary domain concept
- Name: Category
- Type: Aggregate Root

### Related concepts

| Name     | Type          | Notes                              |
|--------|---------------|------------------------------------|
| Product| Aggregate     | Referenced by Category ID          |
| CategoryId | Value Object | Identity                           |

---

## 4. Aggregate Boundary

### Aggregate Root
- Category

### Inside boundary
- ParentCategoryId (Value Object)

### Outside boundary (reference only)
- Product
- Inventory
- Pricing

---

## 5. Invariants

- A category may have at most one parent
- A category cannot be its own ancestor
- Root categories have no parent
- Deleting a category must respect child handling rules
- Moving a category must preserve tree consistency

---

## 6. Domain Operations

| Operation | Description | Enforces Invariants |
|---------|------------|---------------------|
| create | Create new category | parent validity |
| update | Update name / metadata | identity immutability |
| move | Change parent category | no cycles |
| delete | Remove category | child handling rules |

---

## 7. Creation vs Reconstitution

### Creation
- Method: `Category.create(...)`
- Validations:
    - Parent exists (if provided)

### Reconstitution
- Method: `new Category(...)`
- Assumptions:
    - Persisted hierarchy is already valid

---

## 8. Persistence & Mapping Strategy

### Repository
- Returns domain Category
- Saves aggregate root only

### Assembler
- CategoryAggregateAssembler required
- Responsible for:
    - Rebuilding hierarchy state
    - Mapping persistence tree data

### Mapping rules
- Repositories do not perform mapping
- Assemblers coordinate all mappers

---

## 9. Application Services

---

## 10. API Endpoints

| Method | Endpoint | Purpose |
|------|---------|---------|
| POST | /api/v1/categories | Create category |
| PUT | /api/v1/categories/{id} | Update category |
| DELETE | /api/v1/categories/{id} | Delete category |
| GET | /api/v1/categories | List root categories |
| GET | /api/v1/categories/{id} | Get category with children |
| GET | /api/v1/categories/tree | Full category tree |
| PATCH | /api/v1/categories/{id}/move | Move category |

---

## 11. Integration & Side Effects

- Category deletion may affect product visibility
- Category move may require cache invalidation
- No cross-aggregate transaction required

---

## 12. Error Scenarios

| Scenario | Handling |
|--------|----------|
| Parent not found | Domain exception |
| Circular hierarchy | Domain exception |
| Category in use | Business exception |

---

## 13. Implementation Order

1. CategoryId (Value Object)
2. Category aggregate
3. Invariant enforcement
4. Repository interface
5. CategoryAggregateAssembler
6. Repository implementation
7. Application services
8. REST controllers
9. Tests

---

## 14. Definition of Done

- [X] All invariants enforced in domain
- [ ] Repositories are persistence-only
- [X] ADR created
- [ ] API documented
