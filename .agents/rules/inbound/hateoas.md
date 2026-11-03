# R9. HATEOAS

Load when adding links, assemblers, root controllers, or HAL configuration.

## Scope & Architectural Placement

- **Platform BFF Boundary Only**: HATEOAS model assemblers and hypermedia navigation affordances belong **strictly to the Platform BFF layer** (`sellercentral`, `marketplace`).
- **Headless Domain Exemption**: Domain service groups (`catalog`, `order`, `account`, `channel`, `workflow`) do NOT expose REST endpoints and do NOT produce HATEOAS `EntityModel` or `PagedModel` representations. They return pure Java records via `*Port` interfaces.

## Implementation

- `@Component`, implements `RepresentationModelAssembler<ResponseDto, EntityModel<ResponseDto>>`.
- Use `linkTo(methodOn(XxxController.class).methodName(...))`. Never manual URL strings except Tier 2 root endpoints.
- Pass `null` for `@RequestBody`, `@RequestHeader`, `Pageable`, and `PagedResourcesAssembler` params in `methodOn()`.

## Assembler Granularity

- Entity-level: `{Entity}ModelAssembler` for shared links reused across endpoints.
- Per-operation: `{Action}{Entity}ModelAssembler` when the response DTO or link set is operation-specific. Prefer this when command/query results have distinct shapes or navigation.
- Both styles are valid. Choose based on whether links/DTO are shared or operation-specific.

## Rel Naming

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

## Self-Link

- `self` only for the canonical GET endpoint that returns the exact representation.
- Command-result DTOs use `get-{entity}` instead of `self`.

## PagedModel Links

- Every `PagedModel` MUST expose `list-{entities}`.
- Add `create-{entity}` when creation is available.
- Add links on `PagedModel` after `pagedAssembler.toModel()`.

## API Discovery Tiers in Platform BFFs

1. Tier 1: `ApiRootController` at `GET /api/seller/v1` (or `/api/marketplace/v1`) links to all resource roots.
2. Tier 2: `{Resource}RootController` links to top-level resources plus cross-resource entry points.
3. Tier 3: Individual resource endpoints.

- Root endpoints return `ResponseEntity<RepresentationModel<?>>` with `MediaTypes.HAL_JSON_VALUE`.

## Bare EntityModel

- `EntityModel.of(dto)` without links is acceptable for bulk/utility/audit endpoints.
- Prefer adding a meaningful navigation link.

## Cross-Domain Link Relations in Platform BFFs

HATEOAS links may point across bounded contexts for UI/workflow discovery. Links are navigation affordances only. The owning module still serves the data and owns the endpoint.

### MUST

- Advertise cross-domain navigation through a published link facade in the owning module: package `com.grab.app.{owner}.api` with `@NamedInterface("api")`.
- Facade class name: `{Owner}ApiLinks`. Methods return `org.springframework.hateoas.Link` via `linkTo(methodOn(...))`. Never hardcoded path strings.
- Consuming modules import only `{owner}.api` types and declare `allowedDependencies = { ..., "{owner}::api" }`.
- Place cross-domain links where the client needs them:
  - Tier 2 Root when the context entry exposes a use case that needs another context.
  - Relevant `PagedModel` / assemblers that surface the create/compose workflow.
- Keep the same `rel` names as the owning context's root. Do not invent parallel rel synonyms for the same endpoint.

### MUST NOT

- Import another module's `internal/` controllers, assemblers, services, or handlers from a consumer.
- Proxy or re-implement another module's list/search/get under the consumer's API path solely to attach a HATEOAS link.
- Hardcode absolute/relative URL strings in assemblers or root controllers for cross-domain endpoints.
- Add `{owner}::api` dependency just in case. Only when a real cross-domain workflow link is required.
- Put business data from another aggregate into the consuming module's response just because a link was added. Link means navigate; query the owner for payload.
