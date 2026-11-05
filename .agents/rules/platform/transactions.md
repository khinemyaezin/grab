# R16. Transactions

Load when adding transactional annotations or deciding where a transaction starts.

## Multi-Schema Transaction Management

The system operates multi-schema database isolation with dedicated `DataSource` and `PlatformTransactionManager` beans per service group:
- `accountTransactionManager` (Schema: `account`)
- `merchandisingTransactionManager` (Schema: `catalog`)
- `channelTransactionManager` (Schema: `channel`)
- `checkoutTransactionManager` (Schema: `"order"`)

## MUST

- Each service group uses dedicated transactional meta-annotations binding to its transaction manager:
  - Write: `@{Group}Transactional` (e.g. `@AccountTransactional`, `@MerchandisingTransactional`, `@ChannelTransactional`, `@CheckoutTransactional`)
  - Read: `@{Group}ReadTransactional` (read-only optimization)
- Demarcate transactions on **CQRS Handlers** (`CommandHandler` / `QueryHandler`) and port / shared-interface adapters only.
- The handler or outbound query adapter demarcates the transactional unit of work. Business decisions inside that unit belong to aggregates/policies.
- For outbound query adapters or inbound port adapters executing multi-step transactions, start the transaction on the adapter method with the corresponding group transactional annotation.

## MUST NOT

- Put `@Transactional` (or group equivalents) on controllers, BFF services, mappers, assemblers, or domain policies.
- Attempt cross-schema distributed transactions (2PC / JTA). Cross-schema data consistency MUST be achieved asynchronously via the Transactional Outbox pattern.
