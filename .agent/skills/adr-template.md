You are a senior software architect. I need to write an Architectural Decision Record (ADR) for my project. There must be two parts, what is the domain bounded context and application architectural design. 

Domain bounded context or aggregate, the adr should explains the domain context what it will do, what is the responsiblity and what is the boundary and context map between other bounded context. Should draw diagram to show the bounded context and context map and explain each property why it is needed to enforce the business. Also explain the life cycle of aggregate with state diagram.

Application architectural design, the adr should be explain the application design, components, interfaces, data flow, integration with other systems.

# [Title] just title not number
---

## 1. The Problem

**What's not working?**  
[Describe the current gap, pain point, or risk in 1-2 sentences.]

**What's at stake?**  
[Why does this decision matter right now? What happens if we don't address it?]

---

## 2. What We Decided

**The core approach:**  
[A single, clear sentence summarizing the overall decision.]

**Key changes:**
- [Change 1]
- [Change 2]
- [Change 3]

**What stays the same:**  
[Briefly note any major systems or processes that remain unaffected by this decision.]

---

## 2.1. Visual Overview

> *Diagrams to understand the architecture at a glance.*

### High-Level Flow / Components

```
[Insert Mermaid flowchart, sequence, or component diagram here]
```

---

## 3. Why This Approach

**Primary reasons:**
1. [Reason 1]
2. [Reason 2]
3. [Reason 3]

---

## 4. Trade-offs

| Pros | Cons |
|-------|-------|
| [Benefit 1] | [Drawback 1] |
| [Benefit 2] | [Drawback 2] |
| [Benefit 3] | [Drawback 3] |

---

## 5. What Needs to Change

**New components/modules to build:**
- [List new code, services, or infrastructure needed.]

**Changes to existing systems:**
- [List updates, deprecations, or migrations required for current systems.]

---

## 6. Implementation Plan

- **Phase 1:** [Immediate first steps]
- **Phase 2:** [Next steps]
- **Phase 3:** [Final rollout steps]

**Rollback strategy:**  
[How do we revert this safely if something goes wrong?]

---

## 7. Related Documents

- [Link to PRD / BRD]
- [Link to related ADR]
- [Link to technical specifications]