package com.grab.outbox.infrastructure.inbox.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

/**
 * Shared mapping for the per-schema {@code processed_event} inbox table. Each consuming module declares a
 * concrete {@code @Entity @Table(name = "processed_event")} subclass so it lands in that module's persistence unit.
 */
@MappedSuperclass
public abstract class JpaProcessedEventEntity {

    @EmbeddedId
    private ProcessedEventKey key;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    protected JpaProcessedEventEntity() {
    }

    protected final void initialize(ProcessedEventKey key, Instant processedAt) {
        this.key = key;
        this.processedAt = processedAt;
    }

    public ProcessedEventKey getKey() {
        return key;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
