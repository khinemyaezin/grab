# Tech Specification: Workflow Engine & Orchestrated Saga

> **Summary:** A durable, event-driven process manager that coordinates cross-module workflows, checkpoints progress, handles timeouts, and requests defined compensations.
> **Classification:** Architectural Pattern / Platform Infrastructure
> **Supporting Modules:** `framework/workflow` / `workflow-infrastructure` / `store/workflows`
> **Related Architecture ADRs:** [ADR-006](../../dev/system/ADR_006-Orchestrated_saga_architecture.md), [ADR-007](../../dev/system/ADR_007-Workflow_framework_internal_design.md)

---

## 1. Why We Need It

### The Problem

Cross-module business operations (like creating a product, syncing inventory, and updating search indexes) require multiple independent database writes. If coordinated via synchronous HTTP or injected beans, a failure in step 3 leaves steps 1 and 2 committed, leading to data inconsistency and partial state. We need a way to reliably orchestrate multi-step flows and reverse (compensate) them if a downstream step fails.

### Technology Decision & Alternatives

| Technology Option | Decision | Evaluation Rationale |
| :--- | :--- | :--- |
| **Durable Process Manager (Saga)** | **Adopted** | Centralizes sequence logic and compensation order. Keeps bounded contexts completely decoupled (they only consume/emit events). |
| Event Choreography (Pure Pub/Sub) | Rejected | Hard to track the overall status of a complex flow; difficult to orchestrate ordered rollbacks across many independent listeners. |
| Two-Phase Commit (2PC) / XA | Rejected | Blocks database resources, doesn't scale in distributed/modular systems, and isn't supported across microservice boundaries. |
| Temporal / Cadence | Deferred | Extremely powerful, but introduces significant infrastructure overhead for a modular monolith. May adopt if moving to microservices. |

### Key Benefits & Trade-offs

**Core Benefits:**
- Bounded contexts remain completely ignorant of the orchestrator; they just listen to commands and emit completion events.
- Crash resilience: If the app crashes midway, the workflow safely resumes from the last checkpoint.
- Defined compensations are collected and dispatched after a failure; the engine records whether they complete.

**Known Trade-offs:**
- Requires maintaining a central workflow database (instances, correlations, signal dedup).
- Eventual consistency latency instead of immediate delivery (processes communicate via the Transactional Outbox).

---

## 2. What It Is

### Core Concept

The Workflow Engine implements the **Process Manager (Orchestrated Saga)** pattern. It evaluates a workflow definition, dispatches a request to a module, waits for a completion signal, and then advances. On failure, it collects compensation requests by walking the defined steps in reverse order and publishes the collected batch. The requests are not gated on sequential acknowledgements; acknowledgement signals update context, and the workflow becomes `COMPENSATED` only when every compensation predicate is satisfied.

```mermaid
flowchart LR
    subgraph CorePattern ["Core Mechanism (Process Manager)"]
        Engine["Workflow Engine"]
        ModA["Module A"]
        ModB["Module B"]

        Engine -->|"1. Dispatch Step 1"| ModA
        ModA -->|"2. Step 1 Completed"| Engine
        Engine -->|"3. Dispatch Step 2"| ModB
        ModB -->|"4. Step 2 Failed"| Engine
        Engine -->|"5. Dispatch Compensate 1"| ModA
    end
```

### Internal Mechanics & Lifecycle

> Visual lifecycle of a `WorkflowInstance` managed by the engine.

```mermaid
stateDiagram-v2
    [*] --> RUNNING : Process Initiated
    RUNNING --> WAITING_EXTERNAL : Step dispatched (Async)
    WAITING_EXTERNAL --> WAITING_EXTERNAL : Step signal received / Next step
    WAITING_EXTERNAL --> COMPLETED : Final step completed

    RUNNING --> COMPENSATING : Step setup error with compensation requests
    WAITING_EXTERNAL --> COMPENSATING : Async step failed / timed out

    COMPENSATING --> COMPENSATING : Compensation requests published / acknowledgements received
    COMPENSATING --> COMPENSATED : All defined compensation predicates satisfied
    COMPENSATING --> FAILED : Compensation timeout
    RUNNING --> FAILED : Failure with no compensation requests
    WAITING_EXTERNAL --> FAILED : Failure with no compensation requests

    COMPLETED --> [*]
    COMPENSATED --> [*]
    FAILED --> [*]
```

---

## 3. How We Use It in This System

### Architecture & Layer Stacking

```mermaid
flowchart TB
    subgraph StoreLayer ["Application Composition Layer (store)"]
        AppConfig["WorkflowProcess Beans"]
        Inbox["Workflow Signal Inbox"]
        Sweeper["Workflow Timeout Sweeper"]
    end

    subgraph InfraLayer ["Infrastructure Adapter Layer (workflow-infrastructure)"]
        TxEngine["TransactionalWorkflowEngine"]
        JpaStore["JpaWorkflowStore"]
        OutboxPub["OutboxWorkflowSignalPublisher"]
    end

    subgraph FrameworkLayer ["Framework Contracts (framework/workflow)"]
        CoreEngine["EventDrivenWorkflowEngine"]
        Ports["WorkflowStore / WorkflowSignalPublisher"]
        Defs["ProcessDefinition / StepDefinition"]
    end

    AppConfig --> Defs
    Inbox --> TxEngine
    Sweeper --> TxEngine
    TxEngine --> CoreEngine
    CoreEngine --> Ports
    JpaStore -.->|"implements"| Ports
    OutboxPub -.->|"implements"| Ports
```

### Component Responsibilities

| Architectural Role | Layer Location | Responsibility |
| :--- | :--- | :--- |
| `ProcessDefinition` | `store/workflows` | Declarative configuration defining the sequence of steps and their compensation rules. |
| `EventDrivenWorkflowEngine` | `framework/workflow` | The pure domain logic of the engine: evaluates definitions, tracks state, and coordinates retries/rollbacks. |
| `TransactionalWorkflowEngine` | `workflow-infrastructure` | Wraps engine calls in a `REQUIRES_NEW` transaction and handles optimistic locking retries. |
| `JpaWorkflowStore` | `workflow-infrastructure` | Persists `WorkflowInstance` state, step checkpoints, and correlation IDs. |
| `WorkflowSignalInbox` | `store/workflows` | Consumes `WorkflowSignalEvent`s published by modules and routes them into the engine via `onSignal`. |

### Runtime Flow

```mermaid
sequenceDiagram
    participant API as Workflows API
    participant Engine as WorkflowEngine
    participant Store as JpaWorkflowStore
    participant Outbox as Workflow Outbox
    participant ModA as Module A
    participant ModB as Module B

    API->>Engine: start(processName)
    Engine->>Store: Save Instance: RUNNING
    Engine->>Outbox: Dispatch Step 1 Command
    Engine->>Store: Checkpoint: WAITING_EXTERNAL (Step 1)

    Outbox->>ModA: Deliver Command
    ModA->>ModA: Commit changes
    ModA->>Engine: Publish Step 1 Completed (via Inbox)

    Engine->>Store: Load Instance (optimistic lock)
    Engine->>Outbox: Dispatch Step 2 Command
    Engine->>Store: Checkpoint: WAITING_EXTERNAL (Step 2)

    Outbox->>ModB: Deliver Command
    ModB->>ModB: Commit changes
    ModB->>Engine: Publish Step 2 Completed (via Inbox)

    Engine->>Store: Save Instance: COMPLETED
    Engine-->>API: Finished
```

---

## 4. Technical Specification & Contracts

Normative specification. An implementation is compliant only when all MUST / MUST NOT statements are satisfied.

### Normative Rules

- **R-01:** The workflow engine **MUST NOT** share a database transaction with the participating business modules (e.g., Catalog, Inventory).
- **R-02:** The workflow engine **MUST NOT** directly invoke `@Service` beans from other bounded contexts. All cross-module communication MUST occur via dispatched outbox events.
- **R-03:** Each `WorkflowInstance` mutation **MUST** use optimistic locking (`version` field) to prevent concurrent signal processing from corrupting state.
- **R-04:** Every step uses its declared `timeout()` or the fixed 10-minute default. The workflow sweeper starts compensation for an expired external step and fails a workflow whose compensation remains pending beyond its timeout.
- **R-05:** Compensation logic **MUST** be idempotent. A compensation action may be dispatched multiple times if the network or process crashes.

### Current Outcome and Compensation Behavior

The engine marks a workflow `FAILED` immediately when a failure yields no compensation requests. When requests exist, it marks the instance `COMPENSATING` and publishes the collected requests together, in reverse step-definition order. It marks the instance `COMPENSATED` only after all compensation predicates are satisfied. If compensation remains pending until the step timeout, the engine terminates `FAILED`; a failure event received while compensating does not itself establish a compensated outcome. Thus `COMPENSATED` means all defined compensations completed, while `FAILED` can retain partially applied business state.

### Storage & Data Contract

```mermaid
erDiagram
    WORKFLOW_INSTANCE {
        string id PK
        string workflow_name
        string status "RUNNING, WAITING_EXTERNAL, COMPLETED, COMPENSATING, COMPENSATED, FAILED"
        string current_step
        string context_data "Serialized JSON"
        long version "Optimistic lock"
        timestamp created_at
        timestamp updated_at
    }
    WORKFLOW_CORRELATION {
        string correlation_key PK
        string workflow_instance_id FK
        string step_name
    }
```

### Configuration Knobs

| Binding / constant | Value | Unit / format | Description |
| :--- | :--- | :--- | :--- |
| `workflows.sweeper.fixed-delay-ms` | `30000` ms fallback; same value in development profile | Milliseconds | Delay between timeout sweeps. |
| `StepDefinition.DEFAULT_TIMEOUT` | 10 | Minutes | Fixed default timeout when a workflow step does not override it. |
| `EventDrivenWorkflowEngine.SWEEP_BATCH` | 50 | Instances | Fixed maximum candidate count per workflow definition and sweep. |
| `TransactionalWorkflowEngine.SIGNAL_ATTEMPTS` | 3 | Attempts | Fixed optimistic-lock retry count for signal processing and resume; not configurable. |

Implementation references: `framework/src/main/java/com/grab/framework/workflow/impl/EventDrivenWorkflowEngine.java`, `framework/src/main/java/com/grab/framework/workflow/StepDefinition.java`, `workflow-infrastructure/src/main/java/com/grab/workflow/infrastructure/service/TransactionalWorkflowEngine.java`, and `store/src/main/java/com/grab/store/workflows/internal/service/WorkflowSweeper.java`.
