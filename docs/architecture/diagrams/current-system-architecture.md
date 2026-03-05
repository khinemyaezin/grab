# System Architecture Diagram

This diagram describes the architecture currently used by the running system.
It reflects the current modular-monolith design.

## Overview

```mermaid
flowchart TB
    Client["Client / Admin UI"]

    subgraph application["Application Layer"]
        Controllers["Controllers"]
        Facades["Facade / Application Services"]
        CQRS["CommandBus / QueryBus"]
    end
    subgraph modules["Modules"]
        Handlers["Command / Query Handlers"]
        Domain["domain\nAggregates / Value Objects / Domain Services"]
        Infra["infrastructure\nRepositories / Queries / Mappers"]
        Tx["Write Transactional / Read Transactional"]
        Outbox["Outbox Table\n(per-module, same DB transaction)"]
    end

    subgraph scheduler["Outbox Scheduler per module"]
        Poller["Scheduled Poller"]
        Publisher["Event Publisher"]
    end
    
    Client --> Controllers
    Controllers --> Facades
    Facades --> CQRS

    CQRS --> Handlers

    Handlers --> Tx
    Handlers --> Domain
    Handlers --> Infra
    
    Tx -- "writes domain events\n(same transaction)" --> Outbox
    Poller -- "polls unpublished events" --> Outbox
    Poller --> Publisher
    Publisher -- "dispatches events\n(in-process / external)" --> Handlers
```

## Write and Read Flow

```mermaid
flowchart TB
    Request["HTTP Request"] --> Controller["Controller"]
    Controller --> Facade["Facade Service"]
    Facade --> Bus["CommandBus or QueryBus"]

    Bus --> Write["Write Handler / Service"]
    Bus --> Read["Read Handler / Service"]

    Write --> WriteTx["Module Write Transaction"]
    WriteTx --> Aggregate["Aggregate / Domain Logic"]
    Aggregate --> Repository["Repository"]
    Repository --> ModuleDb["Module Persistence Unit"]
    WriteTx -- "insert domain events\n(same TX)" --> OutboxTable["Outbox Table"]
    OutboxTable --> ModuleDb

    Scheduler["Outbox Scheduler"] -- "poll unpublished events" --> OutboxTable
    Scheduler -- "publish & mark delivered" --> EventBus["Event Publisher"]

    Read --> ReadTx["Module Read-Only Transaction"]
    ReadTx --> QueryRepo["Projection / Query Repository"]
    QueryRepo --> ModuleDb
```

## Notes

- The application is a modular monolith.
- Each module is isolated by `datasource`, `entity manager factory`, and `transaction manager`.
- CQRS is in-process, not distributed.
- Each module's write transaction inserts domain events into a per-module **outbox table** within the same database transaction, guaranteeing at-least-once delivery.
- Each **outbox scheduler** periodically polls outbox tables per module for unpublished events, publishes them (in-process or to an external broker), and marks them as delivered.
- This outbox pattern ensures reliable event propagation without two-phase commits across module boundaries.
- The concrete outbox comparison and proposed implementation shape are documented in [ADR-007](../decisions/ADR-007-module-scoped-outbox.md) and [module-scoped-outbox.md](module-scoped-outbox.md).
