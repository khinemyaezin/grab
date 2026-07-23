# ADR-007: Workflow Framework Internal Design

## Status
Accepted (July 22, 2026) — includes resume-from-checkpoint

## Context

ADR-006 defines **when** to use orchestrated workflows vs event choreography.
This ADR documents **how** the workflow kit is structured internally:

- `com.grab.framework.workflow` — portable runner, instance model, store port
- `workflow-infrastructure` — durable `WorkflowStore` JPA persistence
- `com.grab.store.workflows` — Modulith module wiring datasource + runner beans

---

## Decision

Keep one durable workflow kit:

| Layer | Ownership | Role |
|---|---|---|
| Framework | `framework/.../workflow` | Types + `DefaultWorkflowRunner` (checkpoint, compensate, **resume**) |
| Infrastructure | `workflow-infrastructure` | `JpaWorkflowStore` + `workflow_instance` entity/mapper |
| Store module | `store/workflows` | Datasource/Flyway wiring + `WorkflowConfiguration` |

`WorkflowRunner` always persists through `WorkflowStore`.

**Resume-from-checkpoint is supported** for `RUNNING` and `WAITING_EXTERNAL`:
- `run(...)` with an existing workflow id / idempotency key continues from the next incomplete step
- `resume(definition, workflowId)` loads the instance and continues the same way
- Completed checkpoints are not re-executed; context is restored from stored JSON
- `COMPLETED` replays success without re-running steps
- Terminal `FAILED` / `COMPENSATED` / `COMPENSATING` cannot be resumed

---

## Class Diagram

```mermaid
classDiagram
    direction TB

    class WorkflowRunner {
        <<interface>>
        +run(definition, context, request) WorkflowResult
        +resume(definition, workflowId) WorkflowResult
    }

    class DefaultWorkflowRunner {
        -workflowStore: WorkflowStore
        -payloadCodec: WorkflowPayloadCodec
        +run(...) WorkflowResult
        +resume(...) WorkflowResult
    }

    class WorkflowDefinition {
        -name: String
        -steps: List~WorkflowStep~
    }

    class WorkflowStep {
        <<interface>>
        +name() String
        +execute(context) Object
        +compensate(context, output) void
    }

    class WorkflowContext {
        -correlationId: String
        -attributes: Map
    }

    class WorkflowRunRequest {
        -workflowId: String
        -idempotencyKey: String?
    }

    class WorkflowResult {
        <<sealed>>
        +Success
        +Failed
    }

    class WorkflowInstance {
        -status: WorkflowStatus
        -checkpoints: List~WorkflowCheckpoint~
        +recordCheckpoint(...)
        +markRunning()
    }

    class WorkflowStatus {
        <<enumeration>>
        RUNNING
        WAITING_EXTERNAL
        COMPLETED
        FAILED
        COMPENSATING
        COMPENSATED
    }

    class WorkflowStore {
        <<interface>>
        +save(instance)
        +findById(workflowId)
        +findByIdempotencyKey(name, key)
    }

    class JpaWorkflowStore
    class InMemoryWorkflowStore

    WorkflowRunner <|.. DefaultWorkflowRunner
    WorkflowStore <|.. JpaWorkflowStore
    WorkflowStore <|.. InMemoryWorkflowStore
    DefaultWorkflowRunner --> WorkflowStore
    DefaultWorkflowRunner --> WorkflowDefinition
    DefaultWorkflowRunner --> WorkflowInstance
    WorkflowDefinition --> WorkflowStep
```

---

## How It Works

### Happy path

```mermaid
sequenceDiagram
    participant Caller
    participant Runner as DefaultWorkflowRunner
    participant Step as WorkflowStep
    participant Store as WorkflowStore

    Caller->>Runner: run(definition, context, request)
    Runner->>Store: save(RUNNING)
    loop each step
        Runner->>Step: execute(context)
        Runner->>Store: save(checkpoint)
    end
    Runner->>Store: save(COMPLETED)
    Runner-->>Caller: Success
```

### Resume from checkpoint

```mermaid
sequenceDiagram
    participant Caller
    participant Runner as DefaultWorkflowRunner
    participant Store as WorkflowStore
    participant Step as WorkflowStep

    Caller->>Runner: resume(definition, workflowId)
    Runner->>Store: findById
    Note over Runner: restore contextJson
    Note over Runner: rebuild completed steps from checkpoints
    Note over Runner: skip already-finished steps
    loop remaining steps
        Runner->>Step: execute(context)
        Runner->>Store: save(checkpoint)
    end
    Runner->>Store: save(COMPLETED)
    Runner-->>Caller: Success
```

### Failure + compensation

```mermaid
sequenceDiagram
    participant Runner as DefaultWorkflowRunner
    participant Step as WorkflowStep
    participant Store as WorkflowStore

    Runner->>Step: execute (fails)
    Runner->>Store: save(COMPENSATING)
    loop completed steps reverse order
        Runner->>Step: compensate(context, output)
    end
    alt all compensations ok
        Runner->>Store: save(COMPENSATED)
    else compensation error
        Runner->>Store: save(FAILED)
    end
```

### Status lifecycle

```mermaid
stateDiagram-v2
    [*] --> RUNNING: start / save
    RUNNING --> COMPLETED: all steps ok
    RUNNING --> COMPENSATING: step throws
    RUNNING --> WAITING_EXTERNAL: park async (optional)
    WAITING_EXTERNAL --> RUNNING: resume()
    RUNNING --> RUNNING: resume() continues remaining steps
    COMPENSATING --> COMPENSATED: reverse compensate ok
    COMPENSATING --> FAILED: compensate failed
    COMPLETED --> [*]
    COMPENSATED --> [*]
    FAILED --> [*]
```

---

## Package Map

```
framework/.../workflow/
  WorkflowRunner (+ resume), DefaultWorkflowRunner, WorkflowStore, …

workflow-infrastructure/
  JpaWorkflowStore, WorkflowInstanceEntity, WorkflowInstanceMapper

store/.../workflows/
  WorkflowsModuleDataSourceConfig, WorkflowConfiguration, Flyway migrations
```

---

## Consequences

**Positive**
- Crash mid-run can continue the same instance without redoing finished steps
- Idempotent `COMPLETED` replay still works
- Explicit `resume(workflowId)` for workers / ops

**Negative / follow-ups**
- Step outputs must be JSON-serializable
- Checkpoint prefix must still match the definition step order/names
- No automatic background worker yet — caller must invoke `run`/`resume`
- Cross-module steps should use shared ports/adapters (ADR-006 Phase 2)

---

## Related

- ADR-006 — when to use workflow orchestration vs choreography
- Flyway: `db/migration/workflows/V0__create_workflow_instance.sql`
