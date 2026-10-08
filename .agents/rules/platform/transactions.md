# R16. Transactions

Load when adding transactional annotations or deciding where a transaction starts.

## MUST

- Each module has its own `DataSource` + `TransactionManager` (multi-datasource).
- Custom meta-annotations: `@{Module}Transactional` (read-write), `@{Module}ReadTransactional` (read-only).
- Transactions at handler level and outbound API query adapter level only.
- Handler or outbound query adapter demarcates the transactional unit of work. Business decisions inside that unit belong to aggregates/policies.
- For outbound query adapters exposing APIs via shared interface ports, start the transaction on the adapter method with `@{Module}ReadTransactional` (or `@{Module}Transactional`).

## MUST NOT

- Put `@Transactional` (or module equivalents) on service, controller, policy, or mapper.
