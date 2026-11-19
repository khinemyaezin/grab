# Workflow Runtime Communication Protocol

The active protocol is defined by the canonical Java ports in `com.grab.app.workflow.port.message`:

- `WorkflowRuntimeCommandPort` starts an exact definition/version, requests cooperative cancellation, and retries eligible incidents.
- `WorkflowRuntimeSignalPort` accepts correlated participant outcomes with message, operation, attempt, phase, and payload-version identity.
- `WorkflowRuntimeQueryPort` exposes execution summaries, node projections, history, and incidents.

Adapters dispatch through the workflow-scoped command and query buses. Runtime state and dispatch intents are persisted in the workflow transaction and published through the existing transactional outbox. No product-specific workflow API or REST controller is active.

See [Custom Ecommerce Workflow Framework](workflow-framework-tech-spec.md) for the detailed contracts and verification scenarios.
