package com.grab.outbox.infrastructure.inbox.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProcessedEventKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "consumer", nullable = false, length = 150, updatable = false)
    private String consumer;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    protected ProcessedEventKey() {
    }

    public ProcessedEventKey(String consumer, UUID eventId) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.eventId = Objects.requireNonNull(eventId, "eventId");
    }

    public String getConsumer() {
        return consumer;
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProcessedEventKey that)) {
            return false;
        }
        return Objects.equals(consumer, that.consumer) && Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumer, eventId);
    }
}
