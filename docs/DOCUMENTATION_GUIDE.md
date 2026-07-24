# Documentation Guide

How to write and read module documentation so business intent, domain model, and application design stay aligned.

## Doc types

| Type | Purpose | Location |
|------|---------|----------|
| **BRD** | Platform-level business vision and capabilities | [`docs/Commerce_Platform.md`](Commerce_Platform.md) |
| **PRD** | Product what/why: users, goals, features, business rules, shared language | `docs/{module}/product-requirements/` |
| **ADR** | Technical how: bounded context, aggregates, attributes, invariants, CQRS, integration | `docs/{module}/architecture/` (cross-cutting: `docs/system/`) |
| **Feature** | Acceptance criteria, user/system flows, data contracts for a slice of work | `docs/features/` or `docs/{module}/features/` |

**Ownership chain:** BRD → PRD (product) → ADR (domain + app) → Feature (delivery slice).

Templates:

- [`.agent/skills/prd-template.md`](../.agent/skills/prd-template.md)
- [`.agent/skills/adr-template.md`](../.agent/skills/adr-template.md)
- [`.agent/skills/feature-template.md`](../.agent/skills/feature-template.md)

Coding structure rules (not a product doc): [`.agent/skills/architectural-template.md`](../.agent/skills/architectural-template.md)

## What belongs where

| Content | Put it in |
|---------|-----------|
| Goals, personas, in/out of scope, feature “must” lists | PRD |
| Ubiquitous language (business term ↔ domain type) | PRD (full) + ADR (short) |
| Business rules with user-visible effect | PRD (map each rule to domain enforcement) |
| Aggregate roots, entities, VOs, attribute rationale | ADR |
| Relationships (owns vs references-by-id vs cross-BC) | ADR |
| Invariants and which method/policy enforces them | ADR |
| Lifecycle state diagrams | ADR (product narrative summary may live in PRD) |
| CQRS flow, handlers, repos, events, outbox | ADR |
| Acceptance criteria, sequence diagrams for a feature | Feature |

The **ADR owns the full DDD model**. The PRD stays product-facing but must use the same language and point every business rule at a domain concept or policy (or “ADR TBD”).

## How to read an ADR (learning a module)

1. **Problem / Decided** — why this design exists  
2. **Responsibility & Boundary** — what this BC owns and does not own  
3. **Ubiquitous Language** — map business words to types  
4. **Context Map + Aggregate Model** — roots, children, foreign ID refs  
5. **Relationships** — owns vs reference-by-id  
6. **Why Each Property Exists** — business reason for each attribute  
7. **Invariants & Policies** — rules and where they are enforced  
8. **Lifecycle** — allowed status transitions  
9. **Application design** — command/query path and integrations  

If something is unclear after that path, the ADR is incomplete — fix the ADR rather than relying only on code archaeology.

## Naming and location

| Kind | Path pattern | Filename |
|------|--------------|----------|
| Module PRD | `docs/{module}/product-requirements/` | `PRD_NNN-Short_Name.en.md` (locale suffix when needed) |
| Module ADR | `docs/{module}/architecture/` | `ADR_NNN-Short_Name.md` |
| System ADR | `docs/system/` | `ADR_NNN-Short_Name.md` |
| Feature | `docs/features/` or `docs/{module}/features/` | `FAT-NNNN_…` / `FEAT_NNN-…` / descriptive name |

- H1 title is the human name only; the number lives in the filename.  
- Prefer `ADR_` / `PRD_` with zero-padded numbers (`001`, `002`).  
- Related Documents must use paths that exist in this repo. Do not invent `docs/BRD/` or `*_SKILLS.md` links.

## Modules

Documented bounded contexts under `docs/`:

- `catalog/`
- `inventory/`
- `identity/`
- `merchant/`
- `system/` (cross-cutting platform ADRs)
- `features/` (cross-module or narrative feature docs)

## Existing docs

Older ADRs and PRDs may not yet follow these templates. **New** docs should follow the skill templates. Migrating legacy docs is optional follow-up work, not required to use the templates.
