# Workflow Hexagonal Packaging

The boot application now exposes only the canonical headless workflow runtime under `com.grab.app.workflow`.

- `framework` contains the pure Java DSL, immutable state, transition engine, and runtime contracts.
- `workflow-infrastructure` contains PostgreSQL/JPA mappings, timer claiming, and outbox dispatch adapters.
- `app` contains the workflow group’s public Java command, signal, and query ports plus scoped CQRS adapters.
- Platform BFFs do not expose product-workflow REST controllers in this phase.

Cross-domain operations use public capability ports or transactional outbox events. Persistence adapters remain intra-module and the workflow group never imports another domain’s internal package.

See [Custom Ecommerce Workflow Framework](workflow-framework-tech-spec.md) for the complete contract, persistence schema, verification matrix, and rollout limits.
