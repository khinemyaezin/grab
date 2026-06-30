# Feature: [Feature Name]

> **Description:** [One or two sentences describing the feature from the user's perspective.]

---

## 1. Business Rules

| # | Rule | Description |
|---|------|-------------|
| R1 | [Rule name] | [Detailed description of the business rule] |
| R2 | [Rule name] | [Detailed description of the business rule] |

---

## 2. Acceptance Criteria

- [ ] AC1: [Criterion description]
- [ ] AC2: [Criterion description]
- [ ] AC3: [Criterion description]

---

## 3. Sequence Diagrams

### 3.1 Happy Path Flow

mermaid diagram

### 3.2 Error Flow

mermaid diagram

### 3.3 Multi-Entity / Cross-Bounded-Context Flow

mermaid diagram

---

## 4. Flow Charts

### 4.1 Workflow

mermaid diagram

### 4.2 Business Logic Flow

mermaid diagram

---

## 5. Data Contracts

### 5.1 Request

```json
{
  "field1": "string",
  "field2": 123,
  "field3": true
}
```

### 5.2 Response (Success)

```json
{
  "id": "uuid",
  "status": "string",
  "data": {}
}
```

### 5.3 Response (Error)

```json
{
  "code": "error.code",
  "message": "Human-readable message",
  "details": {}
}
```

---

## 6. Related Documents

- [PRD: [Link]](../PRD_SKILLS.md)
- [ADR: [Link]](../ADR_SKILLS.md)
- [Architecture: [Link]](../SKILLS.md)
