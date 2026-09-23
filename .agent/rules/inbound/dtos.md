# R8. DTOs

Load when creating or changing request/response DTOs.

## MUST

- All DTOs are Java `record` types.
- Request DTOs use Jakarta Bean Validation (`@NotBlank`, `@Min`, and similar).
- Response DTOs use primitive types and `String` only.
- Controllers accept and return DTOs.

## MUST NOT

- Put domain types on response DTOs.
- Accept or return domain aggregates or JPA entities from controllers.
