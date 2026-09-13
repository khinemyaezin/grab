# ADR-007: Workflow Framework Internal Design

## Status
Updated September 12, 2026: outbox delivery uses a hot queue after commit; the `@Scheduled` processor is the cold fallback (ADR-011).

Production runs use `EventDrivenWorkflowEngine`. `DefaultWorkflowRunner` is deprecated and is not a bean.

## Context

ADR-006 says **when** to use an orchestrated workflow.
This ADR says **how** the workflow kit is built.

Three places own the kit:

- `com.grab.framework.workflow` — engine, definitions, ports
- `workflow-infrastructure` — JPA store, correlations, signal log, workflow outbox
- `com.grab.store.workflows` — API, process beans, inbox, sweeper

The engine must survive a crash after commit.
It must not inject another bounded context.
It must not share a transaction with catalog, pricing, or inventory.

---

## Decision

Keep **one** durable async process-manager engine.

Authors declare a `WorkflowProcess<C>`.
That bean returns a `ProcessDefinition<C>` of `StepDefinition<C>` steps.
The engine owns persistence, correlation, checkpointing, signaling, compensation, timeout, and resume.

| Layer | Package | Owns |
|---|---|---|
| Framework | `framework/.../workflow` | Ports, engine logic, definition types, instance model |
| Infrastructure | `workflow-infrastructure` | JPA store, workflow outbox, `TransactionalWorkflowEngine` |
| Application | `store/workflows` | REST, `WorkflowProcess` beans, inbox, sweeper |
| Bounded contexts | catalog / pricing / inventory | Request listeners, commands, completion signals |

`DefaultWorkflowRunner` / `WorkflowRunner` stay in the tree as deprecated sync code.
Do not wire them.
Do not use them for new workflows.

Cross-BC traffic stays on `com.grab.store.workflows.events`.
Engine → module events go through the **workflow outbox**.
Module → engine events go through the **module outbox**.
In-JVM dispatch is only the transport.
Durability comes from committed outbox rows.

---

## Layer Diagram

Framework has no Spring transactions and no JPA.
Infrastructure adapts the ports to the workflows database.
The store module is the composition root.
Other modules never call the engine.
They emit `WorkflowSignalEvent`s through their own outbox.

```mermaid
flowchart TB
    subgraph Framework["Framework — com.grab.framework.workflow"]
        EnginePort["WorkflowEngine"]
        Core["EventDrivenWorkflowEngine"]
        Defs["WorkflowProcess / ProcessDefinition / StepDefinition"]
        StorePort["WorkflowStore"]
        PubPort["WorkflowSignalPublisher"]
        Model["WorkflowInstance / InboundSignal / WorkflowSignalEvent"]
        EnginePort --- Core
        Core --> StorePort
        Core --> PubPort
        Core --> Defs
        Core --> Model
    end

    subgraph Infra["Infrastructure — workflow-infrastructure"]
        TxEngine["TransactionalWorkflowEngine"]
        JpaStore["JpaWorkflowStore"]
        OutboxPub["OutboxWorkflowSignalPublisher"]
        Tables[("workflow_instance\nworkflow_correlation\nworkflow_signal_log\nworkflow_outbox_events")]
        TxEngine --> Core
        JpaStore --> Tables
        OutboxPub --> Tables
    end

    subgraph App["Application — store/workflows"]
        Config["WorkflowConfiguration"]
        Registry["WorkflowDefinitionRegistry"]
        Rest["REST start / get"]
        Inbox["WorkflowSignalInbox"]
        Sweep["WorkflowSweeper"]
        ProcessBean["@Component WorkflowProcess"]
        Config --> Registry
        ProcessBean --> Registry
        Rest --> TxEngine
        Inbox --> TxEngine
        Sweep --> TxEngine
        Registry --> Defs
    end

    subgraph BCs["Bounded contexts"]
        Listeners["Module listeners"]
        Cmd["CommandBus"]
        ModOutbox["Module outbox"]
        Listeners --> Cmd
        Listeners --> ModOutbox
    end

    TxEngine -.->|implements| EnginePort
    JpaStore -.->|implements| StorePort
    OutboxPub -.->|implements| PubPort
    OutboxPub -->|"request / compensate events"| Listeners
    ModOutbox -->|"WorkflowSignalEvent"| Inbox
```

---

## Framework Design

The framework is a small kernel.
It does not know Spring, JPA, or any merchant module.

`WorkflowEngine` is the only runtime API:

- `start` — create a run, or return the existing one for the same idempotency key
- `onSignal` — apply a completion, failure, or compensation ack
- `resume` — re-publish the current step's events after a crash
- `sweep` — time out idle `WAITING_EXTERNAL` / `COMPENSATING` rows

`EventDrivenWorkflowEngine` is a plain class.
`TransactionalWorkflowEngine` wraps it in infrastructure and opens a workflows transaction.

Definitions are data plus callbacks.
`WorkflowProcess.create()` builds the graph once.
`ProcessDefinition` is an ordered list of steps.
Each `StepDefinition` says how to enter, complete, correlate, compensate, and time out.

Ports keep persistence and publishing out of the engine:

- `WorkflowStore` — instance, correlation, signal dedup, resumable query
- `WorkflowSignalPublisher` — enqueue events for other modules
- `WorkflowLifecycleListener` — notify the app when a run reaches a terminal status

Signals enter as `WorkflowSignalEvent`.
`InboundSignal.of(event)` copies type, workflow id or correlation, and the dedup key.
The engine looks up the definition from `instance.workflowName()`.
The inbox does not switch on event types.

```mermaid
classDiagram
    direction TB

    class WorkflowEngine {
        <<interface>>
        +start(definition, input, idempotencyKey) WorkflowInstance
        +onSignal(signal) List~WorkflowInstance~
        +resume(workflowId) Optional~WorkflowInstance~
        +sweep()
    }

    class EventDrivenWorkflowEngine {
        +enterFrom()
        +handleSignal()
        +beginCompensation()
    }

    class TransactionalWorkflowEngine {
        +REQUIRES_NEW workflows TX
        +retry on optimistic lock
    }

    class WorkflowProcess~C~ {
        <<interface>>
        +create() ProcessDefinition~C~
    }

    class ProcessDefinition~C~ {
        +name
        +contextType
        +steps
    }

    class StepDefinition~C~ {
        <<interface>>
        +onEnter()
        +onSignal()
        +isComplete()
        +correlationsOnEnter()
        +compensate()
        +onCompensationAck()
        +isCompensated()
        +timeout()
    }

    class WorkflowStore {
        <<interface>>
        +save()
        +findById()
        +findByIdempotencyKey()
        +putCorrelation()
        +tryRecordSignal()
        +findResumable()
    }

    class WorkflowSignalPublisher {
        <<interface>>
        +publish(workflowId, events)
    }

    class WorkflowInstance {
        +id
        +status
        +currentStep
        +contextJson
        +version
    }

    class WorkflowSignalEvent {
        <<interface>>
        +signalType()
        +signalDedupKey()
        +signalWorkflowId()
        +signalCorrelation()
    }

    class InboundSignal {
        +type
        +workflowId
        +correlationKey
        +dedupKey
        +of(WorkflowSignalEvent)
    }

    WorkflowEngine <|.. EventDrivenWorkflowEngine
    WorkflowEngine <|.. TransactionalWorkflowEngine
    TransactionalWorkflowEngine --> EventDrivenWorkflowEngine : delegates
    WorkflowProcess --> ProcessDefinition : create()
    ProcessDefinition --> StepDefinition
    EventDrivenWorkflowEngine --> WorkflowStore
    EventDrivenWorkflowEngine --> WorkflowSignalPublisher
    EventDrivenWorkflowEngine --> WorkflowInstance
    InboundSignal ..> WorkflowSignalEvent : built from
```

---

## Application Layer Design

`store/workflows` is the composition root for the engine.
It does not own catalog, pricing, or inventory writes.

Spring collects `WorkflowProcess` beans the same way it collects `CommandHandler` beans.
`WorkflowConfiguration` builds `WorkflowDefinitionRegistry` from that list.
A new workflow is a `@Component`.
It does not need a new `@Bean` method in the config class.

The REST service starts a run with `process.create()`.
It returns `202` plus the instance.
Get reads the store. It does not call other modules.

`WorkflowSignalInbox` listens on `WorkflowSignalEvent`.
It calls `engine.onSignal`.
Adding a workflow does not change the inbox.

`WorkflowSweeper` is a scheduled job.
It calls `engine.sweep()`.

Module listeners live in their own BC.
They map a request event to a local command.
They write completion or failure events with `WorkflowSignalEmitter`.
They never import another BC's internals.

```mermaid
flowchart TB
    subgraph WorkflowsApp["store/workflows"]
        API["Controller"]
        Svc["WorkflowService"]
        EventListener["WorkflowSignalListener"]
        Sweep["WorkflowSweeper"]
        Events["workflows.events"]
        EngineTx["WorkflowEngine"]
    end
    subgraph Inventory["DomainServices"]
        InvL["Request listener"]
        InvCmd["CommandBus"]
    end
    API --> Svc
    Svc -- create WorkflowProcess \n with Definition --> EngineTx
    EventListener <-- listen on --> Events
    Sweep --> EngineTx
    InvL --> InvCmd
    Events --> InvL
    InvL -- completion / failure --> Events
    EngineTx -- emit signal --> EventListener
```

---

## Flow Diagram

### Happy path

The client gets an accepted response before any module work runs.
Each later hop commits its own outbox, then the next hop consumes it.

```mermaid
sequenceDiagram
    participant Client
    participant API as Workflows API
    participant Engine as WorkflowEngine
    participant WStore as WorkflowStore
    participant WOut as workflow_outbox
    participant Module as BC listener
    participant MOut as module outbox
    participant Inbox as WorkflowSignalInbox

    Client->>API: POST start
    API->>Engine: start(definition, context, idempotencyKey)
    Engine->>WStore: save WAITING_EXTERNAL
    Engine->>WOut: enqueue onEnter events
    API-->>Client: 202 + workflowId

    WOut->>Module: request event
    Module->>Module: CommandBus write
    Module->>MOut: completion event
    MOut->>Inbox: WorkflowSignalEvent
    Inbox->>Engine: onSignal
    Engine->>WStore: checkpoint / next step / COMPLETED
    Engine->>WOut: enqueue next onEnter if needed
```

`WorkflowSignalInbox` does not list event classes.
Each event carries its own signal type, dedup key, and either a workflow id or a correlation key.

A module step and its completion signal commit together.
A failure signal commits in a new transaction, because the step transaction is already rollback-only.
See `WorkflowSignalEmitter`.

### Compensation

A failure or timeout moves the instance to `COMPENSATING`.
The engine publishes each step's `compensate()` events through the workflow outbox.
It stays `COMPENSATING` until every step is `isCompensated` **and** `compensate()` returns empty.
Publishing a compensate request is not proof the work was undone.
A timeout while compensating marks `FAILED`.

### Status lifecycle

```mermaid
stateDiagram-v2
    [*] --> RUNNING: start
    RUNNING --> WAITING_EXTERNAL: enter async step
    WAITING_EXTERNAL --> WAITING_EXTERNAL: fan-in progress
    WAITING_EXTERNAL --> COMPLETED: last step complete
    WAITING_EXTERNAL --> COMPENSATING: failure or timeout
    COMPENSATING --> COMPENSATED: all compensate acks
    COMPENSATING --> FAILED: compensate timeout or nothing to undo
    COMPLETED --> [*]
    COMPENSATED --> [*]
    FAILED --> [*]
```

---

## Transaction Boundary

There is no distributed transaction.
The workflows database and each module database commit on their own.
Outbox rows are the only link.

`TransactionalWorkflowEngine` always opens `PROPAGATION_REQUIRES_NEW` on `workflowsTransactionManager`.
`start`, `onSignal`, `resume`, and `sweep` each get a fresh workflows transaction.
`onSignal` and `resume` retry up to three times on `WorkflowOptimisticLockException`.

### TX 1 — start / signal / resume / sweep

Instance row, correlations, signal log, and workflow outbox rows commit together.
If the process crashes after this commit, the outbox processor still delivers the events.

```mermaid
flowchart TB
    subgraph Tx1["TX 1 — workflows datasource, REQUIRES_NEW"]
        direction TB
        Call["engine.start / onSignal / resume / sweep"]
        Inst["workflow_instance + version"]
        Corr["workflow_correlation"]
        Dedup["workflow_signal_log"]
        WOut["workflow_outbox_events"]
        Call --> Inst
        Call --> Corr
        Call --> Dedup
        Call --> WOut
    end
    Tx1 -->|"after commit"| Hot["workflow hot queue"]
    Hot --> Workers["relay workers"]
    Workers -->|"in-JVM event"| Listener["BC request listener"]
    Poller["@Scheduled poller"] -->|"cold queue"| Workers
```

### TX 2 — module success

The module write and the completion outbox row share the module transaction.
The engine is not in this transaction.
If the command fails, this transaction rolls back and no completion is published.

```mermaid
flowchart TB
    subgraph Tx2["TX 2 — module datasource"]
        direction TB
        Work["CommandBus + aggregate write"]
        MOut["module outbox: completion"]
        Work --> MOut
    end
    Emitter["WorkflowSignalEmitter.runStep"] --> Tx2
    Tx2 -->|"after commit"| Hot["module hot queue"]
    Hot --> Workers["relay workers"]
    Workers -->|"WorkflowSignalEvent"| Inbox["WorkflowSignalInbox"]
    Poller["@Scheduled poller"] -->|"cold queue"| Workers
    Inbox -->|"TX 1 again"| Engine["engine.onSignal"]
```

### TX 3 — module failure

The failed step transaction is rollback-only.
A new transaction writes the failure event so the engine can still compensate.

```mermaid
flowchart TB
    subgraph Tx2Fail["TX 2 — rolled back"]
        FailCmd["command throws"]
    end
    subgraph Tx3["TX 3 — module datasource, REQUIRES_NEW"]
        FailOut["module outbox: failure signal"]
    end
    FailCmd -->|"catch"| Tx3
    Tx3 -->|"after commit"| HotFail["module hot queue"]
    HotFail --> WorkersFail["relay workers"]
    WorkersFail -->|"WorkflowSignalEvent"| Inbox2["WorkflowSignalInbox"]
    Inbox2 -->|"TX 1"| Compensate["engine begins COMPENSATING"]
```

Rules:

- Never join a workflows transaction to a module transaction.
- Never publish a completion from a doomed transaction.
- Never treat an in-memory `ApplicationEventPublisher` call as durable. Persist the outbox row first.

---

## Persistence Diagram

```mermaid
erDiagram
    WORKFLOW_INSTANCE {
        string id PK
        string workflow_name
        string status
        string current_step
        string idempotency_key
        text context_json
        text checkpoint_json
        bigint version
        timestamp updated_at
    }

    WORKFLOW_CORRELATION {
        string workflow_name PK
        string correlation_key PK
        string workflow_id FK
        string step
    }

    WORKFLOW_SIGNAL_LOG {
        string workflow_id PK
        string step PK
        string dedup_key PK
        timestamp recorded_at
    }

    WORKFLOW_OUTBOX_EVENTS {
        bigint id PK
        string aggregate_type
        string aggregate_id
        string payload
        string status
    }

    WORKFLOW_INSTANCE ||--o{ WORKFLOW_CORRELATION : "waiting keys"
    WORKFLOW_INSTANCE ||--o{ WORKFLOW_SIGNAL_LOG : "dedup per step"
    WORKFLOW_INSTANCE ||--o{ WORKFLOW_OUTBOX_EVENTS : "aggregate_id = workflowId"
```

`workflow_correlation` is scoped by workflow name so two workflows can wait on the same business key.
`workflow_instance.version` is the optimistic lock.
`workflow_signal_log` inserts with `ON CONFLICT DO NOTHING` so a duplicate signal does not poison the transaction.

---

## Package Map

```
framework/.../workflow/
  WorkflowEngine, EventDrivenWorkflowEngine
  WorkflowProcess, ProcessDefinition, StepDefinition
  InboundSignal, WorkflowSignalEvent, CorrelationKey
  WorkflowDefinitionRegistry, WorkflowStore, WorkflowInstance

workflow-infrastructure/
  TransactionalWorkflowEngine
  JpaWorkflowStore, WorkflowInstanceEntity (@Version)
  workflow_correlation, workflow_signal_log
  OutboxWorkflowSignalPublisher, WorkflowOutboxEventProcessor

outbox-infrastructure/
  DualQueueOutboxRelay, SpringOutboxCommitHook

store/.../workflows/
  WorkflowConfiguration          — registry from List<WorkflowProcess<?>>
  WorkflowSignalInbox            — one listener on WorkflowSignalEvent
  WorkflowSweeper
  WorkflowTerminalLifecycleListener
  WorkflowProcess beans          — e.g. CreateSellableProductDefinition
  rest/                          — start / get only
  events/                        — shared request, completion, failure types

store/.../{catalog|pricing|inventory}/internal/event/
  listeners + WorkflowSignalEmitter
```

---

## Consequences

**Positive**
- One engine. A new workflow is a `WorkflowProcess` bean plus thin module listeners.
- Completions find the run by `workflowId` or `workflow_correlation`. No full-table scan.
- Optimistic lock on `workflow_instance.version`. Dedup on `workflow_signal_log`.
- Compensation is confirmed before `COMPENSATED`.
- Sweeper resumes or times out stuck `WAITING_EXTERNAL` / `COMPENSATING` rows.
- A crash after persist and before dispatch is recoverable from the outbox.
- Workflows transactions never mix with module transactions.

**Negative / follow-ups**
- Context JSON must stay Jackson-serializable.
- Update-sellable-product and update-product-variant still use hand-written orchestrators until they migrate.
- Inventory compensation stays an explicit no-op where the workflow docs say so.

---

## Related

- ADR-006 — when to use orchestration vs choreography
- ADR-002 — module-scoped transactional outbox
- ADR-011 — outbox hot queue (afterCommit wake + poller fallback)
- ADR-009 — terminal UI notifications from `WorkflowLifecycleListener`
- Flyway: `db/migration/workflows/V0__create_workflow_instance.sql`, `V1__workflow_engine.sql`
