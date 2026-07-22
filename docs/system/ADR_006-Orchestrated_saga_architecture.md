# Choreography and Orchestration Architecture

---

## 1. The Problem

**What's not working?**  
In our Spring Modulith platform, cross-module business operations requiring multiple writes were previously coordinated using in-request workflows that directly injected foreign module step beans (`WorkflowStep`). This pattern violated bounded context encapsulation, leaked module `internal` packages across domain boundaries, tightly coupled modules, and prevented clean extraction to independent microservices.

**What's at stake?**  
Without a clean separation of module boundaries and coordination patterns, cross-module operations cause domain logic fragmentation, fragile transaction management across independent `DataSource` boundaries, and unpredictable failure cascades. Continuing to couple bounded context internals increases architectural complexity and blocks future service decomposition.

---

## 2. What We Decided

**The core approach:**  
Adopt two complementary cross-module coordination patterns chosen strictly by use-case requirements: **Process Manager** (ports + persistent process store for sequence and compensation ownership) and **Event Choreography** (transactional outbox events for independent reactions).

**Key changes:**
- **Decouple Module Internals via Ports:** Replace foreign step bean injections with dedicated Process Manager orchestration. Process Managers communicate with bounded contexts exclusively through coarse-grained capability interfaces (ports) located in `store/shared/process/`.
- **Module Adapters:** Introduce port adapters in `store/{module}/internal/process/adapter/` that translate port interface invocations into module-private `CommandBus` dispatches.
- **Durable Workflow Store:** Introduce `workflow-infrastructure` for durable `WorkflowStore` persistence, wired by the `store/workflows` Modulith module. Resume-from-checkpoint is supported for `RUNNING` / `WAITING_EXTERNAL` (ADR-007).
- **Event Choreography for Decoupled Reactions:** Use module-scoped Transactional Outbox (ADR-002) integration events for event-driven reactions where no single central process owner is required.

**What stays the same:**
- **Single-Module Mutations:** Single-module write operations remain strictly within local CQRS command handlers and module-scoped database transactions (`@{Module}Transactional`).
- **Domain Invariants:** Aggregate invariants and domain validation rules stay inside their respective bounded contexts; Process Managers and event listeners only sequence work.
- **Outbox Infrastructure:** The module-scoped Transactional Outbox (ADR-002) continues to be the foundation for publishing domain and integration events.

---

## 2.1. Visual Overview

> *Diagrams illustrating the domain bounded contexts, context map, aggregate state lifecycle, and component interactions.*

### Bounded Contexts & Context Map

```mermaid
flowchart TD
  subgraph Workflows_BC ["Workflows Bounded Context"]
    PM["Process Manager (Orchestrator)"]
    PS[("Workflow Store Persistence")]
    PM --> PS
  end

  subgraph Workflow_Infra ["workflow-infrastructure"]
    JpaStore["JpaWorkflowStore"]
  end

  PS -.-> JpaStore

  subgraph Shared_Contracts ["Shared Process Contracts (store/shared/process)"]
    PortA["Module A Port Interface"]
    PortB["Module B Port Interface"]
  end

  subgraph ModuleA_BC ["Module A Bounded Context"]
    AdapterA["Module A Port Adapter"]
    CmdA["Internal CommandBus & Handlers"]
    AggA["Domain Aggregate A"]
    AdapterA --> CmdA --> AggA
  end

  subgraph ModuleB_BC ["Module B Bounded Context"]
    AdapterB["Module B Port Adapter"]
    CmdB["Internal CommandBus & Handlers"]
    AggB["Domain Aggregate B"]
    AdapterB --> CmdB --> AggB
  end

  PM --> PortA
  PM --> PortB
  AdapterA -.->|implements| PortA
  AdapterB -.->|implements| PortB
```

### High-Level Flow / Pattern Comparison

```mermaid
flowchart LR
  subgraph PM_Pattern ["Process Manager Pattern (Centralized Sequence & Compensation)"]
    PM["ProcessManager"]
    A1["Module A Port"]
    B1["Module B Port"]
    PM -->|"1. Execution Request"| A1
    A1 -->|"2. Step Completion / Status"| PM
    PM -->|"3. Execution Request"| B1
    B1 -->|"4. Step Completion / Status"| PM
  end

  subgraph Choreo_Pattern ["Choreography Pattern (Decoupled Reactive Listeners)"]
    A2["Module A"]
    E["Transactional Outbox / Event Bus"]
    B2["Module B Event Listener"]
    C2["Module C Event Listener"]
    A2 -->|"1. Commit Aggregate + Outbox Event"| E
    E -->|"2. Event Reaction (Local Command)"| B2
    E -->|"3. Event Reaction (Local Command)"| C2
  end
```

### Process Manager Aggregate / Lifecycle State Diagram

```mermaid
stateDiagram-v2
  [*] --> RUNNING: Process Initiated
  RUNNING --> WAITING_STEP1: Dispatch Step 1 (Async) / Checkpoint
  WAITING_STEP1 --> WAITING_STEP2: Step 1 Event Received / Checkpoint
  WAITING_STEP2 --> COMPLETED: Step 2 Event Received / Checkpoint
  
  RUNNING --> COMPENSATING: Step Execution Error
  WAITING_STEP1 --> COMPENSATING: Step 1 Failed or Timed Out
  WAITING_STEP2 --> COMPENSATING: Step 2 Failed or Timed Out

  COMPENSATING --> COMPENSATED: Reverse Rollback Completed
  COMPENSATING --> FAILED: Compensation Rollback Failed

  COMPLETED --> [*]
  COMPENSATED --> [*]
  FAILED --> [*]
```

### Process Manager Sequence - Synchronous Port Execution

```mermaid
sequenceDiagram
  participant Client
  participant API as Workflows API
  participant PM as ProcessManager
  participant Store as WorkflowStore
  participant A as ModuleAPort
  participant B as ModuleBPort

  Client->>API: POST /api/v1/workflows/start
  API->>PM: start(context)
  PM->>Store: Persist Status: RUNNING
  PM->>A: executeStep1(context)
  A-->>PM: Step 1 Result DTO
  PM->>Store: Checkpoint: STEP1_COMPLETED
  PM->>B: executeStep2(result1)
  B-->>PM: Step 2 Result DTO
  PM->>Store: Persist Status: COMPLETED
  PM-->>API: Process Finished
  API-->>Client: 201 Created + Result DTO
```

### Process Manager Sequence - Asynchronous Event/Outbox Messaging

```mermaid
sequenceDiagram
  participant Client
  participant PM as ProcessManager
  participant Store as WorkflowStore
  participant OB as Transactional Outbox
  participant A as Module A Handler
  participant B as Module B Handler

  Client->>PM: POST /api/v1/workflows/start-async
  PM->>Store: Persist Status: WAITING_STEP1
  PM->>OB: Publish Step1 Command Message
  PM-->>Client: 202 Accepted (processId)

  OB->>A: Deliver Command Message
  A->>OB: Commit Action & Publish Step1Completed Event
  OB->>PM: Handle Step1Completed Event
  PM->>Store: Checkpoint & Status: WAITING_STEP2
  PM->>OB: Publish Step2 Command Message

  OB->>B: Deliver Command Message
  B->>OB: Commit Action & Publish Step2Completed Event
  OB->>PM: Handle Step2Completed Event
  PM->>Store: COMPLETED

  Client->>PM: GET /api/v1/workflows/{processId}
  PM-->>Client: 200 OK (COMPLETED + Result payload)
```

---

## 3. Why This Approach

**Primary reasons:**
1. **Strict Bounded Context Decoupling:** Process Managers operate against shared port interfaces (`store/shared/process/`), preventing modules from importing each other's `internal` domain packages. Adapters map port invocations to private `CommandBus` dispatches.
2. **Clear Use-Case Selection Criteria:** Provides explicit architectural guidance for choosing between Process Manager (when a single orchestrator must own the sequence, compensation order, and status) vs Event Choreography (when modules react independently to facts with eventual consistency).
3. **Resilience & Flexible Transports:** The persistent `WorkflowStore` records checkpoints; `DefaultWorkflowRunner` can resume `RUNNING` / `WAITING_EXTERNAL` instances from the next incomplete step (ADR-007). Synchronous in-request orchestration with compensate-on-failure remains the default path.

---

## 4. Trade-offs

| Pros | Cons |
|-------|-------|
| **Encapsulated Bounded Contexts:** Modules expose coarse capability ports without leaking aggregate structures or internal CQRS command implementations. | **Workflow Store Management:** Requires maintaining dedicated workflow persistence tables, correlation IDs, and checkpoint logic in `workflow-infrastructure`. |
| **Seamless Microservice Migration:** Port interfaces can easily transition from local Spring bean calls to HTTP/gRPC clients or broker queues without refactoring Process Manager logic. | **Distributed Rollback Complexity:** Choreography requires distributed compensating event logic across listeners, making unified process tracking more complex. |
| **Dual Transport Support:** Cleanly handles synchronous immediate response requests as well as long-running asynchronous execution flows. | **Eventual Consistency Latency:** Asynchronous outbox messaging introduces eventual consistency delays between step execution events. |

---

## 5. What Needs to Change

**New components/modules to build:**
- `workflow-infrastructure/`: Durable `JpaWorkflowStore` persistence for workflow execution state.
- `store/workflows/`: Modulith module wiring datasource/Flyway and workflow runner beans; later orchestration entry points.
- `store/shared/process/`: Define shared port interfaces, capability contracts, execution status enums, and handoff DTOs (Process Manager pattern ports).
- `framework/workflow/`: Core workflow abstractions — context, definition, step, result, `WorkflowStore`, and checkpointing `WorkflowRunner`.

**Changes to existing systems:**
- `store/{module}/internal/process/adapter/`: Implement module-specific port adapters that implement shared interfaces from `store/shared/process/` and delegate to local `CommandBus` instances.
- **Deprecate Direct Step Injections:** Remove all cross-module foreign step bean imports (`::workflow`) across bounded context boundaries.
- **Outbox Event Integration:** Standardize module integration events published through the Transactional Outbox (ADR-002) for event-driven Choreography flows.

---

## 6. Implementation Plan

- **Phase 1:** Establish core workflow framework in `framework/workflow/` (checkpointing `WorkflowRunner` + `WorkflowStore` port), durable persistence in `workflow-infrastructure/`, and Modulith wiring in `store/workflows/` with a dedicated `workflows` database. *(Landed: `com.grab.framework.workflow.*`, `workflow-infrastructure` `JpaWorkflowStore`, Flyway `workflow_instance`, `workflows.datasource` — ensure `MODULE_DATABASES` includes `workflows`. Legacy non-durable runner removed.)*
- **Phase 2:** Introduce shared port contracts in `store/shared/process/`, build module port adapters (`store/{module}/internal/process/adapter/`), and migrate multi-step write flows (e.g. CreateSellableItem) to workflow orchestration or Event Choreography.
- **Phase 3:** Remove deprecated cross-module step bean imports, audit bounded contexts for strict `internal` encapsulation, and establish standard use-case pattern selection guidelines for new features.
