You are a product/engineering author writing a Product Requirements Document (PRD).

The PRD stays **product-facing**: problems, users, goals, features, and business rules.
It MUST share vocabulary with the domain model and map each business rule to a domain concept or policy — without embedding full class diagrams or CQRS design (that belongs in the ADR).

**Author rules:**
- Fill **Ubiquitous Language** so PMs and developers use the same words.
- Write **Business Rules** as a table: business statement → domain enforcement → user-visible effect.
- Keep **Domain Concepts** narrative and lifecycle in business terms; link the owning ADR for the technical model.
- Keep **Technical Considerations** thin (constraints only); point to the ADR for structure.
- **Related Documents** must use real repo paths (ADR, BRD, features). Do not link to `docs/BRD/`, `ADR_SKILLS.md`, or `PRD_SKILLS.md`.

Templates for authors: `.agent/skills/adr-template.md`, `.agent/skills/feature-template.md`
Authoring guide: `docs/DOCUMENTATION_GUIDE.md`

---

# Product Requirements Document: [Module/Feature Name]

## 1. Summary

[High-level summary: scope and purpose of this module or feature.]

## 2. Problem

[Specific problems this module solves. What happens without it?]

## 3. Users

[Target users or personas.]
- [e.g., Merchant applicant]
- [e.g., Platform reviewer]
- [e.g., Customer]

## 4. Goals

[Primary goals and objectives.]
- [Goal 1]
- [Goal 2]

## 5. In Scope

[Features and requirements explicitly included.]
- [In-scope item 1]
- [In-scope item 2]

## 6. Out of Scope

[Explicitly excluded items; name the owning module when known.]
- [Out-of-scope item 1 — owned by Identity / Catalog / …]
- [Out-of-scope item 2]

## 7. Ubiquitous Language

> Shared vocabulary. Business term must match how the ADR and code name the concept.

| Business term | Meaning for users / ops | Domain type or value | Notes |
|---------------|-------------------------|----------------------|-------|
| [e.g. Merchant account] | [Business the seller operates under] | `MerchantAccount` | Source of truth in Merchant |
| [e.g. Approved] | [Allowed to operate as a seller] | `MerchantStatus.APPROVED` | Not a user login status |
| [e.g. Owner] | [Applicant after approval] | owner via Identity access | Access owned by Identity |

## 8. Domain Concepts (Product View)

> Short narrative for readers who need the model without opening the ADR. No class diagrams here.

**Main concepts:**
- [Concept A] — [what it represents in the business]
- [Concept B] — [what it represents]

**Lifecycle (business terms):**
- [e.g. Draft → Submitted → Approved / Rejected → Suspended / Closed]

**Boundaries (product language):**
- This module **is** the source of truth for: […]
- This module **is not** responsible for: […] (see [other module])

Technical aggregate design, attributes, invariants, and CQRS flow: see Related Documents → ADR.

## 9. Main Features

[Detail core features. One subsection per major feature.]

### 9.1 [Feature Name]

[Describe the feature from the user’s perspective.]

It must:
- [Requirement 1]
- [Requirement 2]

### 9.2 [Feature Name]

[Describe the feature.]

It must:
- [Requirement 1]
- [Requirement 2]

## 10. Business Rules

> Every rule that the product insists on. Tie each rule to domain enforcement (aggregate method, policy, or “ADR TBD” until designed).

| ID | Business statement | Domain enforcement | User-visible effect |
|----|--------------------|--------------------|---------------------|
| BR1 | [e.g. An applicant may only view and edit their own applications] | `[Aggregate]` method / `XxxAccessPolicy` / ADR TBD | [403 / empty list / …] |
| BR2 | [e.g. Incomplete profiles cannot be submitted] | `[Aggregate].submit()` | [Validation error listing missing fields] |
| BR3 | [e.g. Suspended merchants cannot create new listings] | event + Catalog policy / ADR TBD | [Create product blocked] |

## 11. Success Criteria

The module or feature is successful when:
- [Success criterion 1]
- [Success criterion 2]

## 12. Technical Considerations & Constraints

> Constraints only — not architecture design. Link the ADR for structure.

- [e.g. Must expose RFC 7807 error responses]
- [e.g. Feature flag `module.enabled` for additive rollout]
- [e.g. Optimistic concurrency on updates]
- Architecture, aggregates, and APIs: see Related Documents → ADR

## 13. Dependencies

- [Other module or external system] — [what we need from it]
- [Dependency 2]

## 14. Related Documents

- Platform BRD: `docs/Commerce_Platform.md`
- Owning ADR(s): `docs/{module}/architecture/ADR_NNN-….md`
- Feature specs: `docs/features/…` or `docs/{module}/features/…`
- Authoring templates: `.agent/skills/prd-template.md`, `.agent/skills/adr-template.md`
