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
    subgraph Module["Infrastructure Module"]
        Handler["Command Handler"]
        
        subgraph Transaction["Transaction Boundary"]
            Repo["Repository"]
            Data["Module Business Tables"]
            Outbox["Module Outbox Table"]
        end
  
        Wrapper["OutboxEventProcessor"]
    end

    subgraph Adapter["Outbox module"]
        Producer["DomainEventProducer"]
        Store["OutboxStore"]
        Processor["AbstractOutboxProcessor"]
        Dispatcher["OutboxEventDispatcher"]
    end

    subgraph Delivery["Delivery Adapters"]
        InProcess["ApplicationEventPublisher"]
    end

    subgraph Consumers["Consumers"]
        Listener["Listeners / Integrations"]
    end


    Handler --> Repo
    Repo --> Data
    Repo -- "append outbox row" --> Outbox
    Repo --> Producer
    Producer --> Store
    Store --> Outbox
    Wrapper --> Processor
    Processor -- "claim / deserialize / publish" --> Dispatcher
    Processor --> Store
    Dispatcher --> Processor

    Dispatcher --> InProcess

    InProcess --> Listener
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

- `framework` contains Spring-free outbox contracts.
- `infrastructure-outbox-spring` contains reusable Spring/JPA outbox mechanics.
- Each module owns its outbox table and scheduler wrapper bean.
- If modules still share one physical database, prefer separate schemas or
  table prefixes to preserve ownership boundaries.

## Extraction Path

```mermaid
flowchart LR
    subgraph CatalogService["Catalog Service"]
        COutbox["catalog_outbox_event"]
        CProc["Catalog Outbox Processor"]
    end

    subgraph InventoryService["Inventory Service"]
        IOutbox["inventory_outbox_event"]
        IProc["Inventory Outbox Processor"]
    end

    Broker["Message Broker"]
    Downstream["Downstream Consumers"]

    CProc --> COutbox
    IProc --> IOutbox
    CProc -- "publish" --> Broker
    IProc -- "publish" --> Broker
    Broker --> Downstream
```
