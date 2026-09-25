# R9. HATEOAS

Load when adding links, assemblers, root controllers, or HAL configuration.

## Implementation

- `@Component`, implements `RepresentationModelAssembler<ResponseDto, EntityModel<ResponseDto>>`
- Use `linkTo(methodOn(XxxController.class).methodName(...))`. Never manual URL strings except Tier 2 root endpoints.
- Pass `null` for `@RequestBody`, `@RequestHeader`, `Pageable`, and `PagedResourcesAssembler` params in `methodOn()`.

## Assembler granularity

- Entity-level: `{Entity}ModelAssembler` for shared links reused across endpoints.
- Per-operation: `{Action}{Entity}ModelAssembler` when the response DTO or link set is operation-specific. Prefer this when command/query results have distinct shapes or navigation.
- Both styles are valid. Choose based on whether links/DTO are shared or operation-specific.

## Rel naming

Use inline string literals. No `LinkRelations` constants class.

| Link type | Rel pattern |
|---|---|
| Self | `self` via `.withSelfRel()` |
| Retrieve | `get-{entity}` |
| List collection | `list-{entities}` |
| Search/filter | `search-{entities}` |
| Create | `create-{entity}` |
| Update | `update-{entity}` |
| Delete | `delete-{entity}` |
| State transition | `{action}-{entity}` |
| Qualified action | `{action}-{entity}-{qualifier}` |
| Subresource | `get-{entity}-{subresource}`, `list-{entity}-{subresources}` |

Rules: kebab-case, action-first, plural for collections, never `paged-*`, never `edit-*`, never bare entity nouns.

## Self-link

- `self` only for the canonical GET endpoint that returns the exact representation.
- Command-result DTOs use `get-{entity}` instead of `self`.

## PagedModel links

- Every `PagedModel` MUST expose `list-{entities}`.
- Add `create-{entity}` when creation is available.
- Add links on `PagedModel` after `pagedAssembler.toModel()`.

## 3-tier API discovery

1. Tier 1: `ApiRootController` at `GET /api/v1` links to all bounded context roots.
2. Tier 2: `{Context}RootController` at `GET /api/v1/{context}` links to top-level resources plus any cross-domain workflow entry links required by that context (via `{owner}::api`).
3. Tier 3: individual resource endpoints.

- New bounded context = new Tier 2 root + update `ApiRootController`.
- Root endpoints return `ResponseEntity<RepresentationModel<?>>` with `MediaTypes.HAL_JSON_VALUE`.

## Bare EntityModel

- `EntityModel.of(dto)` without links is acceptable for bulk/utility/audit endpoints.
- Prefer adding a meaningful navigation link.

## Cross-domain link relations

HATEOAS links may point across bounded contexts for UI/workflow discovery. Links are navigation affordances only. The owning module still serves the data and owns the endpoint.

| Concern | Owner |
|---|---|
| Endpoint URI + controller | Owning bounded context |
| Data returned by that endpoint | Owning bounded context |
| Advertising the link from another context's response | Consuming module via published `{owner}::api` facade |
| Local read-model / projection for validation | Consuming module. Not a substitute for the owner's list/search API. |

### MUST

- Advertise cross-domain navigation through a published link facade in the owning module: package `com.grab.store.{owner}.api` with `@NamedInterface("api")`.
- Facade class name: `{Owner}ApiLinks`. Methods return `org.springframework.hateoas.Link` via `linkTo(methodOn(...))`. Never hardcoded path strings.
- Consuming modules import only `{owner}.api` types and declare `allowedDependencies = { ..., "{owner}::api" }`.
- Place cross-domain links where the client needs them:
  - Tier 2 `{Consumer}RootController` when the context entry exposes a use case that needs another context.
  - Relevant `PagedModel` / assemblers that surface the create/compose workflow.
- Keep the same `rel` names as the owning context's root. Do not invent parallel rel synonyms for the same endpoint.
- Prefer linking to the owning Tier 2 root (`get-{owner}-root`) only as a last resort when a specific published link does not exist yet.

### MUST NOT

- Import another module's `internal/` controllers, assemblers, services, or handlers from a consumer.
- Proxy or re-implement another module's list/search/get under the consumer's API path solely to attach a HATEOAS link.
- Expose a local projection as the primary browse API for UI picking. Use the owning catalog/module links instead.
- Hardcode absolute/relative URL strings in assemblers or root controllers for cross-domain endpoints.
- Add `{owner}::api` dependency just in case. Only when a real cross-domain workflow link is required.
- Put business data from another aggregate into the consuming module's response just because a link was added. Link means navigate; query the owner for payload.

### Published `{Owner}ApiLinks` shape

```java
// com.grab.store.{owner}.api — @NamedInterface("api") on package-info.java
public final class CatalogApiLinks {
    private CatalogApiLinks() {}

    public static Link searchProducts() {
        return linkTo(methodOn(ProductController.class).getProducts(null, null, null))
                .withRel("search-products");
    }

    public static Link getProduct() {
        return linkTo(methodOn(ProductController.class).getProduct(null))
                .withRel("get-product");
    }
}
```

- Facade may reference the owner's own `internal/.../controller` types. That is within the owning module. Consumers never touch those controllers.
- Expand the facade when new stable entry-point links are needed by other modules. Keep methods coarse (search/get/create of top-level resources), not every sub-action.

### Client discovery

- `GET /api/v1/{consumer}` (or a workflow `PagedModel`) exposes `_links.{owner-rel}` that point at the owning module endpoint, and `_links.create-{entity}` for the consumer write endpoint.
- Frontend MUST follow `_links` hrefs. MUST NOT hardcode cross-module paths when those rels are present.

### Workflow composition

- When a use case needs many links from several modules, prefer a thin workflow / composition resource under `shared` or a dedicated non-domain package, for example `GET /api/v1/workflows/create-inventory-item`.
- Do not stuff unrelated foreign links onto every consumer resource representation.
- Do not elevate domain handlers into workflow orchestrators for HATEOAS-only concerns.

## HAL configuration (R18)

- No custom `HalConfiguration` bean, no `CurieProvider`, no Affordances API.
- Only relevant config: `server.forward-headers-strategy: framework` (correct URLs behind proxies).
- Dependency: `spring-boot-starter-hateoas`.
