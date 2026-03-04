# Module-Scoped Outbox Design

This diagram describes the proposed outbox design for the current modular
monolith.

The design assumes:

- module-owned persistence boundaries remain explicit
- outbox writes are atomic with module writes
- outbox delivery is asynchronous and retryable

## Overview

```mermaid
flowchart TB
    subgraph Module["Module"]
        Handler["Command Handler"]
        Repo["Repository"]
        Data["tables"]
        Outbox[".outbox_event"]
        Processor["Outbox Processor"]
    end

    subgraph Framework["Shared Outbox Framework"]
        Scheduler["Shared Scheduler"]
        Serializer["Outbox Serializer"]
        Dispatcher["Outbox Dispatcher"]
        Retry["Retry / Cleanup Policy"]
    end

    subgraph Delivery["Delivery Targets"]
        InProcess["ApplicationEventPublisher"]
        Broker["Future Broker Adapter"]
        Consumers["Listeners / Integrations"]
    end

    CQRS --> Handler

    Handler -- "Transaction" --> Repo
    Repo --> Data
    Repo -- "append outbox row\nin same transaction" --> Outbox

    Scheduler --> Processor
    Serializer --> Processor
    
    Retry --> Processor
    
    Processor --> Outbox
    Processor -- "claim / deserialize / publish" --> Dispatcher

    Dispatcher --> InProcess
    Dispatcher --> Broker
    InProcess --> Consumers
    Broker --> Consumers
```

## Publish Lifecycle

```mermaid
sequenceDiagram
    participant H as Module Handler
    participant R as Module Repository
    participant DB as Module Tables
    participant O as Module Outbox
    participant P as Outbox Processor
    participant D as Dispatcher
    participant C as Consumer

    H->>R: Save aggregate
    R->>DB: Persist business state
    R->>O: Insert outbox rows
    Note over DB,O: Same module transaction
    R-->>H: Commit succeeds

    P->>O: Poll available rows
    P->>O: Claim batch
    P->>D: Deserialize and dispatch
    D->>C: Deliver event
    P->>O: Mark published
```

## Notes

- Each module owns its outbox table and repository.
- Scheduler infrastructure can be shared even though processors are
  module-specific.
- If modules still share one physical database, prefer separate schemas or
  table prefixes to preserve ownership boundaries.
