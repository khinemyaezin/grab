You are writing a Tech Specification (Architecture Pattern Specification): a living technical specification for one platform technology, infrastructure pattern, or cross-cutting mechanism.

A Tech Spec is a **contract**. Sections 1–3 explain the rationale and mechanics. Section 4 states what must be true for an implementation to be compliant.

**Flow:** Why We Need It → What It Is → How We Use It in This System → Specification & Contracts.

**Author rules:**
- Use role names in diagrams (e.g. `Event Producer`, `Outbox Poller`, `Message Dispatcher`). Never Java class types or database table names.
- Required diagrams:
  1. Concept flowchart (core pattern abstraction).
  2. State lifecycle diagram (omit if completely stateless).
  3. Project architecture stacking flowchart (how layers interact: `framework` → `infrastructure` → `store`).
  4. Runtime flow sequence diagram.
- Cap each diagram at ~7–9 nodes. No messy tangled graphs.
- No complex massive sentences. Explain simply and show it with diagrams and tables.
- Section 4 uses normative MUST / MUST NOT statements. Knobs are logical names and defaults.
- Replace every `[placeholder]` before publishing.

---

# Tech Specification: [Technology / Pattern Name]

> **Summary:** [One clear sentence anyone on the team can repeat explaining what this tech does.]
> **Classification:** [Platform Infrastructure / Architectural Pattern / Cross-Cutting Concern]
> **Supporting Modules:** `[e.g. framework / outbox-infrastructure / store]`
> **Related Architecture ADRs:** [[ADR Reference]](../../dev/system/[file.md])

---

## 1. Why We Need It

### The Problem

[What breaks or degrades without this technology — concrete, direct, under 150 words.]

### Technology Decision & Alternatives

| Technology Option | Decision | Evaluation Rationale |
| :--- | :--- | :--- |
| **[Chosen Option]** | **Adopted** | [Why this is the best fit for our architecture] |
| [Alternative A] | Rejected | [Why rejected: complexity, coupling, lack of fit, etc.] |
| [Alternative B] | Deferred | [Under what future conditions we would reconsider] |

### Key Benefits & Trade-offs

**Core Benefits:**
- [Benefit 1: State the behavior the implementation demonstrates and its limits]
- [Benefit 2: State a verified benefit without implying an unsupported guarantee]

**Known Trade-offs:**
- [Trade-off 1: e.g. Eventual consistency latency instead of immediate delivery]
- [Trade-off 2: e.g. Requires background poller management and database storage]

---

## 2. What It Is

### Core Concept

[The pattern in plain language without project-specific module names.]

```mermaid
flowchart LR
    subgraph CorePattern ["Core Mechanism"]
        Source["[Caller / Producer]"]
        Buffer["[Transactional Buffer]"]
        Processor["[Processor]"]
        Sink["[External Consumer / Target]"]

        Source -->|"1. Save atomically"| Buffer
        Processor -->|"2. Read & claim"| Buffer
        Processor -->|"3. Dispatch"| Sink
    end
```

### Internal Mechanics & Lifecycle

> Visual lifecycle for stateful entities or tasks managed by this technology.

```mermaid
stateDiagram-v2
    [*] --> [INITIAL_STATUS] : [Implemented creation trigger]
    [INITIAL_STATUS] --> [IN_PROGRESS_STATUS] : [Implemented claim or start]
    [IN_PROGRESS_STATUS] --> [SUCCESS_STATUS] : [Implemented success condition]
    [IN_PROGRESS_STATUS] --> [FAILURE_STATUS] : [Implemented failure condition]
    [FAILURE_STATUS] --> [RETRY_OR_TERMINAL_STATUS] : [Implemented retry or terminal rule]
```

---

## 3. How We Use It in This System

### Architecture & Layer Stacking

> Show how this capability lives across repository layers. Use role names.

```mermaid
flowchart TB
    subgraph StoreLayer ["Application Composition Layer (store)"]
        ClientApp["Command Handler / Listener"]
    end

    subgraph InfraLayer ["Infrastructure Adapter Layer (*-infrastructure)"]
        StoreAdapter["Persistent Store Adapter"]
        WorkerScheduler["Background Worker / Scheduler"]
    end

    subgraph FrameworkLayer ["Framework Contracts (framework)"]
        PortInterface["Core Engine Port / Interface"]
        DataContract["Shared Event / Message Contract"]
    end

    ClientApp --> PortInterface
    StoreAdapter --> PortInterface
    WorkerScheduler --> StoreAdapter
    StoreAdapter --> DataContract
```

### Component Responsibilities

| Architectural Role | Layer Location | Responsibility |
| :--- | :--- | :--- |
| `[Producer / Caller]` | `store` or domain | Initiates the action within the local transaction boundary. |
| `[Engine / Core]` | `framework` | Defines contracts, state machines, and pure processing logic. |
| `[Persistence Adapter]` | `*-infrastructure` | Manages database persistence, schema bindings, and queries. |
| `[Dispatcher / Poller]` | `*-infrastructure` | Polls, claims, and delivers messages to external sinks. |

### Runtime Flow

> Show a realistic request trace through the stacked architecture.

```mermaid
sequenceDiagram
    participant App as Application Handler
    participant Port as Engine Port (Framework)
    participant Store as Persistent Store (Infra)
    participant Worker as Background Processor (Infra)
    participant Target as External Target / Consumer

    App->>Port: Submit payload
    Port->>Store: Save record within caller transaction
    Note over Store: Transaction commits
    Worker->>Store: Claim pending records
    Worker->>Target: Dispatch payload
    Target-->>Worker: Acknowledge success
    Worker->>Store: Mark record completed
```

---

## 4. Technical Specification & Contracts

Normative specification. An implementation is compliant only when all MUST / MUST NOT statements are satisfied.

### Normative Rules

- **R-01:** The system **MUST** persist state changes atomically within the initiating command's database transaction.
- **R-02:** The system **MUST NOT** perform remote network I/O inside the active database transaction boundary.
- **R-03:** State the implemented retry and duplicate-delivery behavior precisely. Do not promise eventual success unless the implementation supports it; identify an attempt cutoff or dead-letter behavior only when it exists.
- **R-04:** If the implementation can redeliver a message, consumers **MUST** be idempotent or handle duplicate delivery safely.

### Storage & Data Contract

```mermaid
erDiagram
    TECHNICAL_RECORD {
        string id PK
        string status "[Actual persisted status values only]"
        string payload "[Actual serialized payload field]"
    }
```

Replace the example fields with the actual persistence contract; remove fields that do not exist.

### Configuration Knobs

List only bindings and constants that exist in the implementation. Show profile overrides separately from code-level fallbacks, and state units explicitly. Do not invent a configuration key, retry limit, terminal status, or default.

| Binding / constant | Implemented default or profile override | Unit / format | Description |
| :--- | :--- | :--- | :--- |
| `[actual.binding.name]` or `[fixed constant]` | [Code fallback / profile override / fixed value] | [Unit] | [Observed behavior; mark unimplemented requirements as gaps.] |
