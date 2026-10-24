You are writing a Feature Specification (Feature Umbrella Architecture Decision Record): the cohesive feature-level architectural decision record that bridges a macro System Design capability phase to detailed modular specifications.

**Author rules:**
- Explain the architectural decision and business problem clearly.
- **Acceptance Criteria (AC) Checklist:** The AC list serves as the testable verification checklist indicating when the architectural decision is fully satisfied. Every criterion must use RFC 2119 keywords (`MUST`, `MUST NOT`) and state concrete, observable behavior (HTTP response codes, security invariants, data constraints, boundary rules) without referencing code-level artifacts.
- **Decomposition:** Decompose the feature into underlying (Domain Specs, Workflow Specs, and Technical Specs). Developers will reference these decision records as the definitive specification to code against.

---

# [Feature Name]
## Participating Bounded Contexts:

### 1. Problem Statement & Architectural Context
[What friction, security vulnerability, scalability bottleneck, or business need does this feature address? Why can't existing platform capabilities or current boundaries satisfy this?]

---

### 2. Acceptance Criteria (Architectural Verification Checklist)

> The Acceptance Criteria list constitutes the testable verification checklist. An implementation satisfies this architectural decision record if and only if all criteria pass.
---

### 3. Underlying Architectural Decision Records (Decomposition)

This feature decision record decomposes into the following detailed micro-decision records that these referenced specifications must be consulted when implementing the solution and those must br contained only related to this feature.

#### Domain Decision Records (Aggregate boundaries, invariants, & entities)
- [[Domain Spec Name]](../spec/domain/{domain}/architecture/{aggregate}-domain-spec.md) — [Summary of domain aggregate decisions]

#### Workflow Decision Records (Cross-domain business orchestration & sagas)
- [[Workflow Spec Name]](../spec/workflows/{workflow-name}.md) — [Summary of cross-aggregate workflow decisions]

#### Technical Decision Records (Platform infrastructure, read models, & protocols)
- [[Tech Spec Name]](../spec/system/{tech-name}-tech-spec.md) — [Summary of technical capability contracts]