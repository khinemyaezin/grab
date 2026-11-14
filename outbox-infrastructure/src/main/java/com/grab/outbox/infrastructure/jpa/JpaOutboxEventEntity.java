package com.grab.outbox.infrastructure.jpa;

import com.grab.framework.outbox.OutboxEntry;
import com.grab.framework.outbox.OutboxStatus;
import com.grab.framework.outbox.SerializedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import java.util.UUID;

/**
 * Shared mapping and state machine for every module's outbox table. Subclasses only declare the table,
 * its indexes and the text columns ({@code headers}, {@code payload}, {@code last_error}) whose column
 * type differs between schemas.
 *
 * <p>All timestamps are {@link Instant}s stored as {@code timestamptz}, so claim staleness and retention do not
 * depend on the JVM time zone.</p>
 */
@MappedSuperclass
public abstract class JpaOutboxEventEntity implements OutboxEntry<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.NEW;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "claim_token")
    private String claimToken;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected JpaOutboxEventEntity() {
    }

    protected final void initialize(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            SerializedEvent serializedEvent,
            Instant now
    ) {
        this.eventId = eventId == null ? UUID.randomUUID() : eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = serializedEvent.eventType();
        this.eventVersion = serializedEvent.eventVersion();
        this.occurredAt = now;
        this.availableAt = now;
        this.status = OutboxStatus.NEW;
        this.attemptCount = 0;
        setHeaders(serializedEvent.headers());
        setPayload(serializedEvent.payload());
    }

    protected abstract void setHeaders(String headers);

    protected abstract void setPayload(String payload);

    public abstract String getLastError();

    protected abstract void setLastError(String lastError);

    @Override
    public void markProcessing(Instant now, String claimToken) {
        status = OutboxStatus.PROCESSING;
        claimedAt = now;
        this.claimToken = claimToken;
        attemptCount += 1;
    }

    @Override
    public void markPublished(Instant now) {
        status = OutboxStatus.PUBLISHED;
        publishedAt = now;
        claimedAt = null;
        claimToken = null;
        setLastError(null);
    }

    @Override
    public void markFailed(String error, Instant nextAvailableAt) {
        status = OutboxStatus.FAILED;
        availableAt = nextAvailableAt;
        claimedAt = null;
        claimToken = null;
        setLastError(error);
    }

    @Override
    public void markDead(Instant now, String error) {
        status = OutboxStatus.DEAD;
        availableAt = now;
        claimedAt = null;
        claimToken = null;
        setLastError(error);
    }

    @Override
    public void releaseClaim(Instant now) {
        status = OutboxStatus.FAILED;
        availableAt = now;
        claimedAt = null;
        claimToken = null;
        attemptCount = Math.max(0, attemptCount - 1);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public int getEventVersion() {
        return eventVersion;
    }

    @Override
    public OutboxStatus getStatus() {
        return status;
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

    public Instant getClaimedAt() {
        return claimedAt;
    }

    @Override
    public String getClaimToken() {
        return claimToken;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }
}
