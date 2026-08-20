# Workflow Terminal UI Notifications

## Status

Accepted (implemented)

**Does not replace:** [ADR_008 — Seller UI event stream](./ADR_008-Seller_ui_event_stream.md). The hub remains transport-only. This ADR is the first domain adapter.

**Does not replace:** [ADR_002 — Module-scoped outbox](./ADR_002-Module_scoped_outbox_architecture.md). Outbox stays the durability domain for inter-module events. UI fan-out is best-effort and process-local.

**Related (frontend):** `grab-web` ADR_005 — Workflow terminal signals on the host event bus.

---

## 1. The Problem

Create-sellable-product returns **202** as soon as the saga parks (`WAITING_EXTERNAL`). Catalog, pricing, and inventory finish later. Without a UI adapter, `SseHub.publish` is never called, so the seller browser can only toast “started”.

---

## 2. What We Decided

Publish a **terminal-only** UI envelope after the workflows transaction commits. The orchestrator is the single writer of workflow status. Named SSE event `workflow`. Statuses: `COMPLETED`, `FAILED`, `COMPENSATED`. Not step progress, not per-SKU domain events.

**Key changes:**

- `CreateSellableProductOrchestrator` publishes `WorkflowTerminalUiEvent` after `markCompleted` / `markFailed` / `markCompensated` and `workflowStore.save`.
- `WorkflowTerminalUiEventListener` is `@TransactionalEventListener(AFTER_COMMIT)` and calls `SseHub.publish(subscriberKey, "workflow", json)`.
- Subscriber key is `platformUserId` + optional `scopeId` from workflow context (`createdBy`, `scopeId`).
- Envelope is small: `producerId`, `workflowId`, `workflowName`, `status`, `productId`, `errorMessage`. REST `GET /{workflowId}` remains the source of truth.

### Envelope

```json
{
  "producerId": "backend",
  "workflowId": "...",
  "workflowName": "create-sellable-product",
  "status": "COMPLETED",
  "productId": "...",
  "errorMessage": null
}
```

---

## 3. Why This Approach

1. AFTER_COMMIT avoids the UI GETting a stale instance.
2. Terminal-only keeps the stream quiet (one event per run).
3. Routing from context matches the SSE hub key used on connect / context reconnect.
4. Later sagas reuse `WorkflowTerminalUiEvent` without changing the hub.

---

## 4. Trade-offs

| Pros | Cons |
|------|------|
| One event per workflow run | No step progress in the UI |
| Process-local hub is enough for a single store instance | Events published while the browser is disconnected are lost until REST refetch |
| Outbox and SSE fail independently | Multi-instance store later needs sticky sessions or Redis fan-out |

---

## 5. Related Documents

- [ADR_008 — Seller UI event stream](./ADR_008-Seller_ui_event_stream.md)
- [ADR_006 — Orchestrated saga](./ADR_006-Orchestrated_saga_architecture.md)
- [ADR_007 — Workflow framework](./ADR_007-Workflow_framework_internal_design.md)
- Frontend ADR_005 (`grab-web` `docs/decisions/ADR_005-WorkflowTerminalSignals.md`)
