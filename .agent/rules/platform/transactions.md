# R13. Transactions

Load when adding transactional annotations or deciding where a transaction starts.

## MUST

- Each module has its own `DataSource` + `TransactionManager` (multi-datasource).
- Custom meta-annotations: `@{Module}Transactional` (read-write), `@{Module}ReadTransactional` (read-only).
- Transactions at handler level only.
- Handler demarcates the transactional unit of work. Business decisions inside that unit belong to aggregates/policies.

## MUST NOT

- Put `@Transactional` (or module equivalents) on service, controller, policy, or mapper.
