You are writing a System Design Document (Macro Architecture Decision Record): a high-level architectural specification for one subsystem, cross-cutting capability, or major platform evolution.

A System Design Document is an **Architecture Decision Record (ADR)** that establishes the subsystem topology, bounded context boundaries, architectural invariants, and core algorithms. It is NOT an implementation task tracker or sprint backlog.

**Author rules:**
- Explain architecture, trade-offs, boundaries, and algorithms.
- Use logical architectural role names and bounded contexts (e.g., `Edge Context Resolver`, `Catalog Projection Engine`, `Pricing Engine`). **NEVER** mention framework class names (`*Service`, `*Adapter`, `*UseCase`), Java annotations, or persistence table schemas.
- In Section 5, decompose the subsystem into sequential **Architectural Capability Phases**. Each phase represents an architectural boundary transition and maps 1:1 to a Feature Decision Record (`docs/features/feat_xxx.md`). Do NOT list code tasks or class names in Section 5.
- Keep diagrams clean and capped at ~7–9 nodes.
- Replace every `[placeholder]` before publishing.

---

# [Document Title: Subsystem & Architecture Decision Record]

## 1. The Problem

### What's Not Working?
[Describe specific architectural and business problems using clear, concise bullet points.]

### What's at Stake?
[A concise summary explaining the business impact, security risk, CDN/cache inconsistency, or performance degradation if this architecture is NOT adopted.]

---

## 2. The Proposed Design

### 2.1 Architecture Overview
[High-level description of the proposed subsystem topology and its place within the platform. Include a simplified Mermaid architecture diagram showing participating bounded contexts.]

### 2.2 Key Components & Architectural Roles
[List and describe the major architectural roles or bounded contexts that make up this subsystem. Use logical role names, never implementation class names.]

| Architectural Role / Boundary | Core Responsibilities | Interactions & Collaborators |
| :--- | :--- | :--- |
| **[Role Name 1]** | [Primary responsibility and boundary authority] | [Collaborating roles, inbound/outbound ports] |
| **[Role Name 2]** | [Primary responsibility and boundary authority] | [Collaborating roles, inbound/outbound ports] |

### 2.3 Data Model & Boundary Ownership
[Describe the primary data structures, ubiquitous language, and boundary ownership across bounded contexts. Highlight how data ownership differs from the legacy design.]

#### Ubiquitous Language & Ownership Matrix
| Bounded Context | Aggregate / Root Concept | Owns | References by ID | Explicitly Does NOT Own |
| :--- | :--- | :--- | :--- | :--- |
| **[Context A]** | `[Root Concept]` | [Attributes and lifecycle states] | `[externalId]` | [Concepts owned by other contexts] |

### 2.4 Key Flows & Core Algorithms
[Walk through the primary end-to-end interactions, data flows, and algorithmic mechanisms. Include Mermaid sequence diagrams and state machines using logical participant roles.]

---

## 3. Implementation Approach

### 3.1 Technology & Tooling Choices
[Explain the technology stack, architectural styles (e.g. Hexagonal, CQRS), and platform tooling. Justify why each choice satisfies the architectural invariants.]

| Technology / Architectural Pattern | Selection Rationale |
| :--- | :--- |
| **[Technology / Pattern 1]** | [Why this is selected and how it enforces boundaries] |

---

## 4. Why This Approach (Technical Rationale)
[Compare the chosen architectural design against concrete alternatives across simplicity, performance, scalability, maintainability, and security.]

| Evaluation Metric | Selected Design | Alternative A | Alternative B |
| :--- | :--- | :--- | :--- |
| **Simplicity** | [Evaluation and rationale] | [Evaluation and rationale] | [Evaluation and rationale] |
| **Performance** | [Evaluation and rationale] | [Evaluation and rationale] | [Evaluation and rationale] |
| **Scalability** | [Evaluation and rationale] | [Evaluation and rationale] | [Evaluation and rationale] |
| **Maintainability** | [Evaluation and rationale] | [Evaluation and rationale] | [Evaluation and rationale] |
| **Security** | [Evaluation and rationale] | [Evaluation and rationale] | [Evaluation and rationale] |

---

## 5. Architectural Capability Decomposition & Feature Decisions

Decompose the subsystem into sequential architectural capability phases. Each phase represents an architectural boundary transition and maps 1:1 to a Feature Decision Record (`docs/features/feat_xxx.md`). 

Do NOT list implementation class names, code tasks, or Jira tickets. Developers reference these feature decision records as the definitive specification to code against.

### Phase 01: [Capability / Boundary Name]
- **Boundary & Scope**: [Which bounded contexts and communication boundaries are established or modified]
- **Architectural Decisions & Invariants**: [Key architectural rules, boundary isolation invariants, and security/consistency policies enforced]
- **Core Algorithms & Design Mechanics**: [Key algorithms, evaluation precedence, or protocols executed in this boundary]
- **Feature Decision Record**: [[feat_00x-feature-name.md](../features/feat_00x-feature-name.md)]

### Phase 02: [Capability / Boundary Name]
- **Boundary & Scope**: [Which bounded contexts and communication boundaries are established or modified]
- **Architectural Decisions & Invariants**: [Key architectural rules, boundary isolation invariants, and security/consistency policies enforced]
- **Core Algorithms & Design Mechanics**: [Key algorithms, evaluation precedence, or protocols executed in this boundary]
- **Feature Decision Record**: [[feat_00y-feature-name.md](../features/feat_00y-feature-name.md)]
