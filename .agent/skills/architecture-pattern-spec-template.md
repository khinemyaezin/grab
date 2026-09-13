You are writing an Architecture Pattern Specification (APS): a living technical specification for one platform technology.

An APS is a **contract**. Sections 1–3 teach. Section 4 states what must be true for an implementation to be correct. It is not an ADR.

**Order:** Why → What it is → How we use it here → Specification.

**Author rules:**
- Role names in pictures. Never Java types or file paths.
- Required diagrams: concept flowchart, lifecycle (omit if stateless), project layers. Cap each at ~9 nodes. No colors or `style` / `classDef` directives.
- Section 4 uses MUST / MUST NOT. Knobs are names and defaults, not YAML keys.
- Do **not** include class tables, `CREATE TABLE`, implementation plan, phases, or rollback.
- No complex massive sentences. explain simple and show it with diagrams.
---

# [Technology / Architecture Pattern Name]

> **Summary:** [One sentence anyone can repeat.]

---

## 1. Why We Need It

### Problem

[What breaks without it — concrete, under 150 words.]

### Why We Chose It

| Option | Decision | Reason |
| :--- | :--- | :--- |
| **[Chosen]** | **Chosen** | [Why] |
| [Alternative] | Rejected | [Why not] |
| [Alternative] | Deferred | [Until when] |

### Key Benefits

- [Benefit 1]
- [Benefit 2]

### Trade-offs

- [Limitation 1]
- [Limitation 2]

---

## 2. What It Is

### Overview

[The pattern in plain language. No project module names yet.]

### Core Concept

### How It Works Internally

### Lifecycle

---

## 3. How We Use It in This Project

### Architecture

[Where we stacked it. Role names, not classes. how this tack live in project.]

### Responsibilities

### Runtime Flow

[What happens on a real request in this platform.]
---

## 4. Specification

Normative. An implementation matches this spec when the compliance list is true.

### Data

[ER diagram, role diagram]

