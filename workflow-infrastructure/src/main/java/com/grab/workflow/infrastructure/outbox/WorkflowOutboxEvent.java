package com.grab.workflow.infrastructure.outbox;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_outbox_events", indexes = {
        @Index(name = "idx_workflow_outbox_status_available", columnList = "status, available_at"),
        @Index(name = "idx_workflow_outbox_claimed_at", columnList = "claimed_at"),
        @Index(name = "idx_workflow_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
})
public class WorkflowOutboxEvent implements OutboxEntry<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Column(columnDefinition = "text", nullable = false)
    private String headers;

    @Column(columnDefinition = "text", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.NEW;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @Column(name = "claim_token")
    private String claimToken;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    public static WorkflowOutboxEvent pending(
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            LocalDateTime now
    ) {
        WorkflowOutboxEvent event = new WorkflowOutboxEvent();
        event.aggregateType = aggregateType;
        event.aggregateId = aggregateId;
        event.eventType = serializedEvent.eventType();
        event.eventVersion = serializedEvent.eventVersion();
        event.headers = serializedEvent.headers();
        event.payload = serializedEvent.payload();
        event.occurredAt = now;
        event.availableAt = now;
        event.status = OutboxStatus.NEW;
        return event;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public int getEventVersion() {
        return eventVersion;
    }

    @Override
    public String getHeaders() {
        return headers;
    }

    @Override
    public OutboxStatus getStatus() {
        return status;
    }

    @Override
    public String getClaimToken() {
        return claimToken;
    }

    @Override
    public void markProcessing(LocalDateTime now, String claimToken) {
        status = OutboxStatus.PROCESSING;
        claimedAt = now;
        this.claimToken = claimToken;
        lastError = null;
        attemptCount += 1;
    }

    @Override
    public void markPublished(LocalDateTime now) {
        status = OutboxStatus.PUBLISHED;
        publishedAt = now;
        claimedAt = null;
        claimToken = null;
        lastError = null;
    }

    @Override
    public void markFailed(LocalDateTime now, String error, Duration retryDelay) {
        status = OutboxStatus.FAILED;
        availableAt = now.plus(retryDelay);
        claimedAt = null;
        claimToken = null;
        lastError = error;
    }
}
