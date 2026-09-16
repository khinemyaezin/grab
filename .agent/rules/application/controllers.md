# R3. Controllers

Load when creating or changing REST controllers.

## MUST

- `@RestController` + `@RequestMapping("/api/v1/{resource}")` + `@RequiredArgsConstructor`
- Inject `XxxCommandService`, `XxxQueryService`, `XxxModelAssembler` (entity-level or per-operation)
- Inject `PagedResourcesAssembler<ResponseDto>` as a method param for paginated endpoints
- Return `ResponseEntity<EntityModel<T>>` (single), `ResponseEntity<PagedModel<EntityModel<T>>>` (paginated), or `ResponseEntity<Void>` plus `Location` header (creation without body)
- Use `@Valid @RequestBody` and `@RequestHeader(value = "X-Actor-Id")`

## MUST NOT

- Put business logic in the controller. Delegate everything to services.
