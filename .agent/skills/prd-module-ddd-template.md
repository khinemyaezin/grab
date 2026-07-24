# Module Name: [e.g., Inventory Management, User Authentication]

## 1. Executive Context & Scope
* **Business Goal:** [1–2 sentences explaining the core business problem this module solves.]
* **Bounded Context (System Boundaries):**
    * **In Scope (What this module OWNS):** [List core data & logic it owns]
    * **Out of Scope (What other modules handle):** [Explicitly state boundaries]
* **Upstream/Downstream Dependencies:**
    * *Upstream (Inputs from):* [e.g., Order Service]
    * *Downstream (Outputs to):* [e.g., Notification Service]

---

## 2. Ubiquitous Language (Domain Dictionary)
> Define core business terms precisely. Everyone (PMs, Engineers, QA) must use these exact terms in conversation and code.

| Term | Business Definition | Code Equivalent Hint |
| :--- | :--- | :--- |
| **[Term 1]** | [Exact business definition] | *Entity / Value Object* |
| **[Term 2]** | [Exact business definition] | *Domain Service / Policy* |

---

## 3. Core Business Logic & Invariants
> Invariants are domain rules that MUST ALWAYS be true. If a rule is broken, the system must reject the operation.

* **Rule 1 (Invariant):** [e.g., An order cannot be shipped if inventory balance is less than 1.]
* **Rule 2 (Policy):** [e.g., Accounts inactive for 90 days are flagged as dormant.]
* **Rule 3 (Validation):** [e.g., User email must be unique across the tenant.]

---

## 4. State Lifecycle & Transitions
> Visual or written map of the core domain entity states and what triggers state changes.
> * **State Definitions:**
* `[State A]`: [What this state means to the business]
* `[State B]`: [What this state means to the business]
* **Transition Rules:**
    * Cannot transition from `[State A]` to `[State C]` directly without passing through `[State B]`.

---

## 5. Main Functional Workflows (Commands & Processes)

### Workflow 1: [Name of Primary Action, e.g., Cancel Subscription]
1. **Trigger / Command:** [Who or what initiates this?]
2. **Inputs:** [List required data payload]
3. **Execution Steps:**
    * Step 1: Validate [Invariant X].
    * Step 2: Execute [Business Rule Y].
    * Step 3: Update state to `[State Z]`.
4. **Expected Output:** [Result returned to caller]
5. **Edge Cases & Failure Modes:**
    * If [Condition A] fails $\rightarrow$ Return error: `[Error Code/Reason]`.

---

## 6. Integrations & Domain Events
> Events emitted by this module when key business milestones occur.

### Emitted Events (What happened)
* `[Entity][Action]Happened` (e.g., `OrderPlaced`, `AccountSuspended`)
    * **Payload:** `[entity_id, timestamp, core_attributes]`
    * **Consumers:** [List which other modules listen to this]

### Consumed Events (What this module listens to)
* `[UpstreamEvent]` $\rightarrow$ Triggers `[Internal Action/Command]`.

---

## 7. Non-Functional Requirements (NFRs)
* **Performance:** [e.g., p95 response time < 200ms]
* **Consistency Level:** [e.g., Strong consistency required vs. Eventual consistency acceptable within 5 seconds]
* **Data Retention:** [e.g., Retain audit logs for 7 years]