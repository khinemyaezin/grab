# Architecture migration

This folder records the layered → hexagonal tightening of the **whole** modular monolith: every Maven module, including workflow, storage, outbox, and `store`.

It does **not** extract microservices. The runtime stays one Spring Boot app (`store`). The change is compile-time layout so use cases are no longer trapped in the composition root, and infrastructure no longer defines ports.

| Document | What it decides |
|----------|-----------------|
| [ADR_001-Hexagonal_module_layout.md](ADR_001-Hexagonal_module_layout.md) | Module kinds (full hex, lite, platform, projector, composition), target trees, Maven and Modulith graphs, where workflow lives |

Related current-state docs:

- [ADR-001 Current System Architecture](../dev/system/ADR_001-System_architecture.md)
- [ADR-007 Workflow Framework](../dev/system/ADR_007-Workflow_framework_internal_design.md)
- [Commerce Platform BRD](../Commerce_Platform.md)

Domain field models stay in `docs/dev/domain/*/architecture/` — this folder does not repeat them.
