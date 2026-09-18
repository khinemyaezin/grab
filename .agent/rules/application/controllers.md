# R3. Controllers

Load when creating or changing REST controllers.

## MUST

- `@RestController` + `@RequestMapping("/api/v1/{resource}")` + `@RequiredArgsConstructor`
- Inject `XxxCommandService`, `XxxQueryService`, `XxxModelAssembler` (entity-level or per-operation)
- Delegate write operations (POST, PUT, PATCH, DELETE) strictly to `XxxCommandService`
- Delegate read operations (GET) strictly to `XxxQueryService`
- Keep write and read paths cleanly separated without mixing responsibilities
- Inject `PagedResourcesAssembler<ResponseDto>` as a method param for paginated endpoints
- Return `ResponseEntity<EntityModel<T>>` (single), `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated), or `ResponseEntity<Void>` plus `Location` header (creation without body)
- Use `@Valid @RequestBody` and `@RequestHeader(value = "X-Actor-Id")`

## MUST NOT

- Put business logic in the controller. Delegate everything to services.
- Inject or invoke `CommandBus` or `QueryBus` directly. Controllers must go through services.
- Inject or call handlers or repositories (domain or infrastructure).
- Call `XxxCommandService` from a GET endpoint or `XxxQueryService` from a state-mutating endpoint.
