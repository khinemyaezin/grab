package com.grab.workflow.infrastructure.outbox;

import com.grab.framework.outbox.SerializedEvent;
import com.grab.outbox.infrastructure.adapter.out.jpa.JpaOutboxEventEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_outbox_events", indexes = {
        @Index(name = "idx_workflow_outbox_status_available", columnList = "status, available_at"),
        @Index(name = "idx_workflow_outbox_claimed_at", columnList = "claimed_at"),
        @Index(name = "idx_workflow_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
})
public class WorkflowOutboxEvent extends JpaOutboxEventEntity {

    @Column(name = "headers", columnDefinition = "text", nullable = false)
    private String headers;

    @Column(name = "payload", columnDefinition = "text", nullable = false)
    private String payload;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    protected WorkflowOutboxEvent() {
    }

    public static WorkflowOutboxEvent pending(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            Instant now
    ) {
        WorkflowOutboxEvent event = new WorkflowOutboxEvent();
        event.initialize(eventId, aggregateType, aggregateId, serializedEvent, now);
        return event;
    }

    @Override
    public String getHeaders() {
        return headers;
    }

    @Override
    protected void setHeaders(String headers) {
        this.headers = headers;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    protected void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    protected void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
