# R6. Command, Query, and Result Records

Load when creating or changing Command, Query, or Result types.

## MUST

- Java `record` implementing `Command<R>` or `Query<R>`
- Use `Id` from framework for entity identifiers, not raw `String`
- Paginated queries: `R = Page<XxxResult>`, and the record also implements `PageableQueryRequest`
- Result records use primitives, `String`, and `Id`

## MUST NOT

- Put domain aggregates on Result records.
