# Feature: Authority-Based Authorization and Actor Migration

## 1. Objective

Replace every trust decision based on `X-Actor-Id` with the verified platform
identity from `SecurityContext`, enforce stable authority checks at HTTP and
method boundaries, and enforce seller ownership in CQRS handlers.

Dynamic roles are administrative groupings. Endpoint code never hardcodes
dynamic role names; it checks stable authority codes supplied by FAT-0004.

---

## 2. Scope

### In scope

- Remove `X-Actor-Id` from all production controllers and tests
- Inject `@AuthenticationPrincipal SecurityPrincipal` into protected endpoints
- Pass platform actor ID and authority snapshot through service/mappers into
  commands and queries
- Add `@PreAuthorize` checks using stable authorities
- Enforce own-resource versus any-resource policy in handlers
- Restrict catalog moderation, audit, and bulk operations
- Restrict inventory read and write operations
- Make HATEOAS action links authority-, ownership-, and state-aware
- Add reusable authenticated-principal test support
- Add endpoint matrix, ownership, and spoofing regression tests

### Out of scope

- Token parsing and verification (FAT-0005)
- Identity, role, and authority persistence (FAT-0004)
- Database row-level security or tenant-specific schemas
- Arbitrary per-resource ACL administration

### Delivery dependency

FAT-0004 must provide dynamic roles, stable authorities, and platform identity
resolution. FAT-0005 must establish the verified `SecurityPrincipal` and
authenticated-by-default filter chain before this migration is enabled.

### Guideline override

The architectural guideline's legacy controller examples use
`@RequestHeader("X-Actor-Id")`. This feature intentionally supersedes that
transport pattern. All other controller, CQRS, mapper, transaction, DTO,
exception, and HATEOAS rules remain mandatory.

---

## 3. Authority Catalog Used by This Feature

| Authority | Scope |
|---|---|
| `PRODUCT_WRITE_OWN` | Create and mutate resources owned by the actor |
| `PRODUCT_WRITE_ANY` | Mutate any seller's product resources |
| `PRODUCT_MODERATE` | Approve, reject, suspend, and restore products |
| `PRODUCT_AUDIT_READ` | Read product audit history |
| `PRODUCT_BULK_WRITE` | Run bulk catalog mutations |
| `INVENTORY_MANAGE_OWN` | Read and mutate inventory owned by the actor |
| `INVENTORY_MANAGE_ANY` | Read and mutate any seller's inventory |

Seed mappings normally grant `*_OWN` to `SELLER` and both `*_OWN` and
`*_ANY` plus administrative authorities to `ADMIN`. Dynamic roles may receive
any subset without endpoint code changes.

---

## 4. Authorization Layers

```text
SecurityFilterChain
  → authenticated-by-default
  → exact public storefront allowlist

@PreAuthorize
  → checks stable operation authority

CQRS handler
  → loads resource and canonical owner
  → permits owner with *_OWN or any actor with *_ANY
  → performs aggregate operation in module transaction
```

Request-level authority checks are necessary but never sufficient for
seller-scoped data. Clients cannot supply, override, or select their actor ID.

---

## 5. X-Actor-Id Migration Inventory

The current production inventory controllers contain 22 header parameters:

| Controller | Production occurrences |
|---|---:|
| `BinController` | 5 |
| `LocationController` | 6 |
| `ZoneController` | 5 |
| `InventoryController` | 6 |

All must be removed. Additionally, protected reads that currently have no
actor header must receive the principal so ownership can be enforced. A final
repository-wide search for `X-Actor-Id` must return no production or test
usage.

### Controller pattern

```java
@PostMapping
@PreAuthorize("hasAnyAuthority('INVENTORY_MANAGE_OWN','INVENTORY_MANAGE_ANY')")
public ResponseEntity<EntityModel<LocationResponse>> createLocation(
        @Valid @RequestBody CreateLocationRequest request,
        @AuthenticationPrincipal SecurityPrincipal principal) {
    LocationResponse response = locationCommandService.createLocation(
            request, principal.actor());
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(locationModelAssembler.toModel(response));
}
```

Controllers contain no ownership logic. They pass the immutable actor to the
command/query service and delegate response assembly.

---

## 6. CQRS Propagation Pattern

### Service and mapper

Each operation keeps the established flow:

```text
Controller(SecurityPrincipal)
  → CommandService(RequestDto, AuthenticatedActor)
  → one operation-specific MapStruct mapper
  → Command(Id actorId, Set<String> authorities, ...)
  → CommandBus
  → transactional handler
```

Operation mappers remain abstract MapStruct classes using
`CentralMapperConfig` and `IdMapper`. Actor IDs are converted to framework
`Id`; raw `String` identifiers are not added to commands or queries.

Example command shape:

```java
public record UpdateLocationCommand(
    Id locationId,
    UpdateLocationData data,
    Id actorId,
    Set<String> authorities
) implements Command<UpdateLocationResult> {}
```

Use immutable defensive copies for authority sets. Result records contain
response data, never `SecurityPrincipal`, domain aggregates, or JPA entities.

### Handler ownership policy

```java
ownershipPolicy.requireOwnOrAny(
        resource.sellerId(),
        command.actorId(),
        command.authorities(),
        "INVENTORY_MANAGE_OWN",
        "INVENTORY_MANAGE_ANY");
```

The handler loads the canonical resource owner. It must not compare against an
owner ID from a request body. Missing ownership authority or a mismatched owner
throws the bounded context's service exception with `ErrorCategory.FORBIDDEN`.

Create operations set owner ID from the actor. For `*_ANY` administrative
creation on behalf of a seller, use a separate explicit administrator endpoint
and command; do not reintroduce a general client-supplied actor field.

---

## 7. Catalog Authorization Matrix

| Operation | Access |
|---|---|
| Product detail by ID/slug, storefront search | Public |
| Create product | `PRODUCT_WRITE_OWN` or `PRODUCT_WRITE_ANY` |
| Update/delete descriptions, media, variants | Own-or-any ownership policy |
| Submit product for review | Own-or-any ownership policy |
| Approve/reject/suspend/restore | `PRODUCT_MODERATE` |
| Product status mutation outside moderation workflow | Explicit matching authority; no generic authenticated access |
| Product audit | `PRODUCT_AUDIT_READ` |
| Bulk upsert | `PRODUCT_BULK_WRITE` |
| Variation matrix utility | Public only if it reads public catalog data; otherwise authenticated explicitly |

Representative annotations:

```java
@PreAuthorize("hasAnyAuthority('PRODUCT_WRITE_OWN','PRODUCT_WRITE_ANY')")
@PreAuthorize("hasAuthority('PRODUCT_MODERATE')")
@PreAuthorize("hasAuthority('PRODUCT_BULK_WRITE')")
@PreAuthorize("hasAuthority('PRODUCT_AUDIT_READ')")
```

Catalog command and query records for seller-owned resources must carry the
actor context just like inventory records. Existing product records must have
a canonical seller/owner ID before these checks can be enabled.

---

## 8. Inventory Authorization Matrix

All location, zone, bin, inventory-item, stock movement, and reservation reads
and writes require either `INVENTORY_MANAGE_OWN` or
`INVENTORY_MANAGE_ANY` unless a separate customer order workflow explicitly
owns the operation.

| Actor | Own resource | Other seller resource |
|---|---:|---:|
| `INVENTORY_MANAGE_OWN` | Allow | 403 |
| `INVENTORY_MANAGE_ANY` | Allow | Allow |
| No matching authority | 403 | 403 |

Repository queries must support owner-scoped lookups and paginated lists.
`listLocations`, inventory lists, movements, reservations, zones, and bins
must filter by actor ownership unless `INVENTORY_MANAGE_ANY` is present.

Avoid “load all then filter” behavior. Apply ownership constraints in the
query repository and still verify ownership in mutation handlers.

---

## 9. Package and File Changes

```text
store/.../shared/security/
├── SecurityPrincipal.java
└── CurrentActorAccessor.java

store/.../catalog/internal/
├── api/rest/controller/ProductController.java
├── api/rest/service/ProductCommandService.java
├── api/rest/service/ProductQueryService.java
├── api/rest/mapper/{Operation}RequestMapper.java
├── command/{Operation}Command.java
├── command/handler/{Operation}CommandHandler.java
├── query/{Operation}Query.java
├── query/handler/{Operation}QueryHandler.java
└── authorization/CatalogOwnershipPolicy.java

store/.../inventory/internal/
├── api/rest/controller/{Location,Zone,Bin,Inventory}Controller.java
├── api/rest/service/*CommandService.java
├── api/rest/service/*QueryService.java
├── api/rest/mapper/{Operation}RequestMapper.java
├── command and query records/handlers
└── authorization/InventoryOwnershipPolicy.java
```

Write handlers retain `@CatalogTransactional` or `@InventoryTransactional`.
Read handlers retain the corresponding read-only transaction annotation.
Authorization is not moved into controllers, mappers, JPA entities, or model
assemblers.

---

## 10. HATEOAS Requirements

Assemblers may inject `CurrentActorAccessor` to decide whether to advertise an
action. Link visibility improves discovery but never replaces server-side
authorization.

- Use `linkTo(methodOn(...))`; no manual URLs outside bounded-context roots.
- Use inline action-explicit rels such as `update-product`, `approve-product`,
  `create-location`, and `list-locations`.
- Use `self` only for the canonical GET representation.
- Add state-transition links only when resource state, ownership, and actor
  authority all allow the transition.
- Every paginated `PagedModel` includes its canonical `list-{entities}` link.
- Add `create-{entity}` only when the current actor can create it.
- Pass `null` for principal, body, pageable, and assembler parameters in
  `methodOn(...)` calls when the value is unavailable.

If the response lacks enough ownership information for a safe decision, omit
the command link rather than advertising an unavailable action.

---

## 11. Error Contract

| Condition | Status | Code |
|---|---:|---|
| Anonymous protected request | 401 | `idt.service.auth.missing_token` |
| Missing operation authority | 403 | `idt.service.auth.access_denied` |
| Seller accesses another seller's resource | 403 | `{module}.service.authorization.not_owner` |
| Authenticated owner references missing resource | 404 | Existing module not-found code |

Do not reveal whether another seller's resource exists when that would leak
sensitive information. For such endpoints, policy may intentionally translate
owner mismatch to 404; the choice must be consistent per resource and tested.

---

## 12. Test Support

```java
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockSecurityPrincipalFactory.class)
public @interface WithMockSecurityPrincipal {
    String platformUserId() default "user-1";
    String issuer() default "https://identity.local.grab";
    String subject() default "user-1";
    String email() default "user@example.com";
    String[] roles() default {"SELLER"};
    String[] authorities() default {"INVENTORY_MANAGE_OWN"};
}
```

Controller slice tests must use a real security context for authorization
cases. `addFilters = false` is allowed only for narrow MVC serialization tests
that do not claim to verify security. Handler tests pass explicit actor IDs and
authority sets and verify repository scoping.

---

## 13. Detailed Implementation Plan

1. **Inventory and freeze the old contract**
   - Record every production/test `X-Actor-Id` occurrence and every protected
     endpoint without actor propagation.
   - Add a temporary architecture test that fails on new header usage.

2. **Introduce actor propagation utilities**
   - Add `SecurityPrincipal.actor()` and `CurrentActorAccessor` in shared
     security.
   - Add `FORBIDDEN` handling to catalog/inventory sealed service errors.
   - Create `CatalogOwnershipPolicy` and `InventoryOwnershipPolicy` with unit
     tests; keep them independent of servlet and Spring Security APIs.

3. **Migrate inventory controllers and CQRS flows**
   - Replace 22 header parameters and add principals to protected reads.
   - Update each command/query service signature.
   - Update one operation-specific mapper per handler to map actor ID to `Id`
     and copy authorities.
   - Update command/query records and handlers, preserving transaction
     annotations and handler `getCommandType()`/`getQueryType()` methods.

4. **Implement inventory repository scoping**
   - Add seller-scoped lookups and pageable query methods.
   - Verify ownership before every mutation and scope every seller list/read.
   - Add indexes required for owner plus common filter columns.

5. **Migrate catalog commands and queries**
   - Add principal propagation to product create/update/delete, variants,
     media, descriptions, review submission, and protected reads.
   - Add or backfill canonical product owner IDs if absent.
   - Apply own-or-any policy in handlers.

6. **Add method-level authority policy**
   - Enable method security through FAT-0005.
   - Annotate controller entry points using the matrix above.
   - Use stable authorities only; remove `hasRole` and `ROLE_` checks.

7. **Update HATEOAS assemblers**
   - Inject current actor access where action visibility is needed.
   - Preserve canonical `self`, collection, cross-controller, and state links.
   - Add authority/ownership/state link tests using the documented rel names.

8. **Migrate tests and remove compatibility behavior**
   - Add `WithMockSecurityPrincipal` and its factory.
   - Replace all request headers with mock principals.
   - Add spoofing tests proving an `X-Actor-Id` header cannot change actor.
   - Remove any fallback from missing principal to request data.

9. **Run architecture and integration verification**
   - Search production and tests for `X-Actor-Id`, `hasRole`, `hasAnyRole`, and
     direct `ROLE_` comparisons; expected result is zero authorization usage.
   - Run module, CQRS, MVC, security, HATEOAS, ownership, and full regression
     tests.

---

## 14. Acceptance Scenarios

| Scenario | Expected result |
|---|---|
| Anonymous calls protected inventory endpoint | RFC 7807 401 |
| Customer without inventory authority calls inventory | RFC 7807 403 |
| Seller reads or mutates own inventory | Allowed |
| Seller reads or mutates another seller's inventory | 403 or documented non-leaking 404 |
| Actor with `INVENTORY_MANAGE_ANY` accesses any inventory | Allowed |
| Seller mutates own product | Allowed |
| Seller mutates another seller's product | 403 or documented non-leaking 404 |
| Moderator approves product | Allowed with `PRODUCT_MODERATE` |
| Dynamic moderator role receives `PRODUCT_MODERATE` | Allowed without code change |
| Role name changes while authority remains | Authorization behavior unchanged |
| Client sends spoofed `X-Actor-Id` | Header ignored; verified actor is used |
| Public storefront request has no token | Allowed |
| Public route receives invalid token | RFC 7807 401 |

---

## 15. Definition of Done

- [ ] No production or test code uses `X-Actor-Id`.
- [ ] Protected controllers receive `SecurityPrincipal` and contain no
      authorization or ownership business logic.
- [ ] Every operation uses its own abstract MapStruct request mapper.
- [ ] Command/query identifiers use framework `Id`.
- [ ] Handlers retain module transaction annotations and enforce ownership.
- [ ] Seller-scoped query repositories filter in the database.
- [ ] No endpoint authorization uses hardcoded role names or `ROLE_` prefixes.
- [ ] Dynamic role-authority changes affect access without redeployment.
- [ ] Product moderation, audit, bulk, product ownership, and inventory
      ownership matrices are covered by tests.
- [ ] HATEOAS action links follow the architectural guideline and omit
      unavailable actions.
- [ ] RFC 7807 401/403 responses use documented error codes.
- [ ] Architecture, handler, repository, controller, security, HATEOAS, and
      full regression tests pass.
