You are writing a System Design Document: a high-level architectural specification to create the business requirement or issue in the system.

A System Design Document is an **Architecture Decision Record (ADR)** that establishes the subsystem topology, bounded context boundaries, architectural invariants, and core algorithms.

**Author rules:**
- Explain architecture, trade-offs, boundaries, and algorithms.
- Use logical architectural role names and bounded contexts (e.g., `Edge Context Resolver`, `Catalog Projection Engine`, `Pricing Engine`).
- In Section 5, decompose the subsystem into sequential **Architectural Capability Phases**. Each phase represents an architectural boundary transition and maps 1:1 to a Feature Decision Record (`docs/features/feat_xxx.md`).
- Keep diagrams clean and capped at ~7–9 nodes.
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

### 2.2 Key Components
- [List and describe the major architectural roles, bounded contexts and algorithm that make up this subsystem.]
- [Walk through the primary end-to-end interactions, data flows, and algorithmic mechanisms. Include Mermaid diagrams to virtualize best.]

---

## 3. Implementation Approach

### 3.1 Technology & Tooling Choices
[Explain the technology stack, architectural styles and platform tooling. Justify why each choice satisfies the architectural invariants.]

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

### Phase XX: [Capability / Boundary Name]
- **Boundary & Scope**: [Which bounded contexts and communication boundaries are established or modified]
- **Architectural Decisions & Invariants**: [Key architectural rules to solve or create for this phase]
- **Core Algorithms & Design Mechanics**: [Key algorithms, evaluation precedence, or protocols executed in this boundary]
- **Feature Decision Record**: [[feat_00x-feature-name.md](../features/feat_00x-feature-name.md)]
