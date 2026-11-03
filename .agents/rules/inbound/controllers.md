# R3. Controllers

Load when creating or changing REST controllers.

## MUST

- **Platform BFF Boundary Only**: `@RestController` resides **exclusively** within Platform BFF modules (`sellercentral`, `marketplace`). Headless domain modules MUST NOT contain REST controllers.
- Route prefixes: `@RequestMapping("/api/seller/v1/{resource}")` (Seller Platform BFF) or `@RequestMapping("/api/marketplace/v1/{resource}")` (Marketplace Platform BFF).
- Constructor injection: `@RequiredArgsConstructor`.
- Inject Universal Java Port interfaces (`*Port`), BFF orchestration services, and `*ModelAssembler`.
- Delegate write operations (POST, PUT, PATCH, DELETE) strictly to `*Port` write methods or `XxxCommandService`.
- Delegate read operations (GET) strictly to `*Port` query methods or `XxxQueryService`.
- Keep write and read paths cleanly separated without mixing responsibilities.
- Inject `PagedResourcesAssembler<ResponseDto>` as a method param for paginated endpoints.
- Return `ResponseEntity<EntityModel<T>>` (single), `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated), or `ResponseEntity<Void>` plus `Location` header (creation without body).
- Use `@Valid @RequestBody` and persona identity headers (e.g. `@RequestHeader(value = "X-Actor-Id")` or security principal injection).
- Apply perimeter authorization annotations (`@PreAuthorize("hasAuthority('...')")`).

## MUST NOT

- **Create REST controllers inside domain service groups (`catalog`, `order`, `account`, `channel`, `workflow`)**. Domain services are strictly headless.
- Put business logic in the controller. Delegate everything to Java ports or BFF services.
- Inject or invoke `CommandBus` or `QueryBus` directly. Controllers must go through Java ports or BFF services.
- Inject or call CQRS handlers, use cases, persistence repositories, or persistence adapters.
- Call write port methods / command services from a GET endpoint, or query port methods / query services from a state-mutating endpoint.
