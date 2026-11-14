package com.grab.outbox.infrastructure;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * In-memory outbox row for unit tests; mirrors the JPA entity state machine.
 */
public final class StubOutboxEvent implements OutboxEntry<Long> {
    private final Long id;
    private final UUID eventId = UUID.randomUUID();
    private final String headers;
    private final Instant occurredAt = Instant.EPOCH;
    private Instant availableAt;
    private OutboxStatus status = OutboxStatus.NEW;
    private String claimToken;
    private int attemptCount;
    private String lastError;

    public StubOutboxEvent(Long id, String headers) {
        this(id, headers, Instant.EPOCH);
    }

    public StubOutboxEvent(Long id, String headers, Instant availableAt) {
        this.id = id;
        this.headers = headers;
        this.availableAt = availableAt;
    }

    public StubOutboxEvent withAttempts(int attempts) {
        this.attemptCount = attempts;
        return this;
    }

    public String lastError() {
        return lastError;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getEventType() {
        return "type";
    }

    @Override
    public String getPayload() {
        return "payload";
    }

    @Override
    public int getEventVersion() {
        return 1;
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
    public int getAttemptCount() {
        return attemptCount;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public Instant getAvailableAt() {
        return availableAt;
    }

    @Override
    public void markProcessing(Instant now, String claimToken) {
        this.status = OutboxStatus.PROCESSING;
        this.claimToken = claimToken;
        this.attemptCount++;
    }

    @Override
    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.claimToken = null;
    }

    @Override
    public void markFailed(String error, Instant nextAvailableAt) {
        this.status = OutboxStatus.FAILED;
        this.claimToken = null;
        this.availableAt = nextAvailableAt;
        this.lastError = error;
    }

    @Override
    public void markDead(Instant now, String error) {
        this.status = OutboxStatus.DEAD;
        this.claimToken = null;
        this.lastError = error;
    }

    @Override
    public void releaseClaim(Instant now) {
        this.status = OutboxStatus.FAILED;
        this.claimToken = null;
        this.availableAt = now;
        this.attemptCount = Math.max(0, attemptCount - 1);
    }
}
